package com.leyou.search.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.JsonUtils;
import com.leyou.common.vo.PageResult;
import com.leyou.item.client.ItemClient;
import com.leyou.item.dto.*;
import com.leyou.search.pojo.Goods;
import com.leyou.search.repository.GoodsRepository;
import com.leyou.search.request.SearchRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.Operator;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.LongTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortMode;
import org.elasticsearch.search.sort.SortOrder;
import org.json.JSONString;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilter;
import org.springframework.data.elasticsearch.core.query.FetchSourceFilterBuilder;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class SearchService {
    @Autowired
    private ItemClient itemClient;

    @Autowired
    private GoodsRepository goodsRepository;

    @Autowired
    private ElasticsearchTemplate esTemplate;

    /**
     * 将数据库的spu对象转换成Goods 对象
     * @param spuDTO
     * @return
     */
    public Goods buildGoods(SpuDTO spuDTO) {
        // 1 商品相关搜索信息的拼接：名称、分类、品牌、规格信息等
        //1.1分类信息
        String categoryNames = itemClient.queryCategoryByIds(spuDTO.getCategoryIds())
                .stream().map(CategoryDTO::getName)
                .collect(Collectors.joining(","));
        //1.2品牌
        BrandDTO brandDTO = itemClient.queryBrandById(spuDTO.getBrandId());
        //1.3名称等 完成拼接
        String all = spuDTO.getName() + categoryNames + brandDTO.getName();

        //2 spu下的所有sku的JSON数据
        List<SkuDTO> skuDTOList = itemClient.querySkuBySpuId(spuDTO.getId());
        List<Map<String, Object>> skuMapList = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", skuDTO.getId());
            map.put("price", skuDTO.getPrice());
            map.put("title", skuDTO.getTitle());
            map.put("image", StringUtils.substringBefore(skuDTO.getImages(), ","));
            skuMapList.add(map);
        }
        // 3 当前spu下所有sku的价格的集合
        Set<Long> price = skuDTOList.stream().map(SkuDTO::getPrice)
                .collect(Collectors.toSet());

        // 4 当前spu的规格参数
        Map<String, Object> specs = new HashMap<>();

        //4.1获取规格参数的key，来自于SpecParam中当前分类下的需要搜索的规格
        List<SpecParamDTO> specParamDTOList = itemClient.querySpecParams(null, spuDTO.getCid3(), true);
        // 4.2 获取规格参数的值，来自于spuDetail
        SpuDetailDTO spuDetailDTO = itemClient.querySpuDetailById(spuDTO.getId());
        //4.2.1通用规格参数值
        Map<Long, Object> genericSpec = JsonUtils.toMap(spuDetailDTO.getGenericSpec(), Long.class, Object.class);
        //4.2.2通用规格参数值
        Map<Long, List<String>> specialSpec = JsonUtils.nativeRead(spuDetailDTO.getSpecialSpec(), new TypeReference<Map<Long, List<String>>>() {
        });

        for (SpecParamDTO specParamDTO : specParamDTOList) {
            //获取规格参数的名称
            String key = specParamDTO.getName();
            //获取规格参数的值
            Object value = null;
            //判断是否为通用属性
            if (specParamDTO.getGeneric()) {
                //通用属性
                value = genericSpec.get(specParamDTO.getId());
            } else {
                //特有属性
                value = specialSpec.get(specParamDTO.getId());
            }
            if (specParamDTO.getNumeric()) {
                //是数字类型 分段
                value = chooseSegment(value, specParamDTO);
            }
            specs.put(key, value);
        }

        Goods goods = new Goods();
        goods.setId(spuDTO.getId());
        goods.setSubTitle(spuDTO.getSubTitle());
        goods.setSkus(JsonUtils.toString(skuMapList));
        goods.setAll(all);
        goods.setBrandId(brandDTO.getId());
        goods.setCategoryId(spuDTO.getCid3());
        goods.setCreateTime(spuDTO.getCreateTime().getTime());
        goods.setPrice(price);
        goods.setSpecs(specs);

        return goods;
    }

    /**
     * 将价格以段 进行保存
     * @param value
     * @param p
     * @return
     */
    private String chooseSegment(Object value, SpecParamDTO p) {
        if (value == null || StringUtils.isBlank(value.toString())) {
            return "其它";
        }
        double val = NumberUtils.toDouble(value.toString());
        String result = "其它";
        //保存数值段
        for (String segment : p.getSegments().split(",")) {
            String[] segs = segment.split("-");
            //获取数值范围
            double begin = NumberUtils.toDouble(segs[0]);
            double end = Double.MAX_VALUE;
            if (segs.length == 2) {
                end = NumberUtils.toDouble(segs[1]);
            }
            //判断是否在范围内
            if (val >= begin && val < end) {
                if (segs.length == 1) {
                    result = segs[0] + p.getUnit() + "以上";
                } else if (begin == 0) {
                    result = segs[1] + p.getUnit() + "以下";
                } else {
                    result = segment + p.getUnit();
                }
                break;
            }
        }
        return result;
    }


    /**
     * 查询搜索框的分页
     * @param request
     * @return
     */
    public PageResult<Goods> search(SearchRequest request) {
        //1.获取请求参数
        String key = request.getKey();
        //判断是够有搜索条件，如果没有，直接返回null，不允许搜索全部商品
        if (StringUtils.isBlank(key)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }

        //2.构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2.1通过sourceFilter设置返回的结果字段，我们只需要id，skus，subTitle
        queryBuilder.withSourceFilter(new FetchSourceFilter(
                new String[]{"id", "skus", "subTitle"}, null));
        //关键字的match匹配
//        queryBuilder.withQuery(QueryBuilders.matchQuery("all", key).operator(Operator.AND)); //改造
        queryBuilder.withQuery(buildBasicQuery(request));
        //2.2排序
        Boolean price = request.getPrice();
        if (price != null) {
            if (price) {
                queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.ASC));
            } else {
                queryBuilder.withSort(SortBuilders.fieldSort("price").order(SortOrder.DESC));
            }
        }

        //2.3分页
        Integer page = request.getPage();
        Integer size = request.getSize();
        queryBuilder.withPageable(PageRequest.of(page - 1, size));


        //3.查询，获得结果
        Page<Goods> result = goodsRepository.search(queryBuilder.build());
        int totalPages = result.getTotalPages();
        long total = result.getTotalElements();
        List<Goods> list = result.getContent();
        //4.分装并返回结果
        return new PageResult<>(total, totalPages, list);


    }

    /**
     * 最基础的查询
     * @param request
     * @return
     */
    private QueryBuilder buildBasicQuery(SearchRequest request) {
        //构建布尔查询
        BoolQueryBuilder queryBuilder = QueryBuilders.boolQuery();
        //构建基本的match查询
        queryBuilder.must(QueryBuilders.matchQuery("all", request.getKey()).operator(Operator.AND));
        //构建过滤条件
        for (Map.Entry<String, String> entry : request.getFilter().entrySet()) {
            String key = entry.getKey();
            if ("分类".equals(key)) {
                key = "categoryId";
            } else if ("品牌".equals(key)) {
                key = "brandId";
            } else {
                key = "specs." + key;
            }
            queryBuilder.filter(QueryBuilders.termQuery(key, entry.getValue()));
        }
        return queryBuilder;
    }


    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 查询栏
     * @param request
     * @return
     */
    public Map<String, List<?>> queryFilter(SearchRequest request)  {
     /*   ObjectMapper objectMapper = new ObjectMapper();
        String requestJson = objectMapper.writeValueAsString(request);
              //先从缓存中拿去数据
        Map<Object, Object> filterList = redisTemplate.opsForHash().entries(requestJson);

        //判断从redis中取到的数据是否存在 不存在就去索引库中去拿
        if (CollectionUtils.isEmpty(filterList)) {

        }*/


        //1.创建过滤条件的map集合
        Map<String, List<?>> filterList = new LinkedHashMap<>();
        //2.查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        //2.1获取查询条件
        QueryBuilder basicQuery = buildBasicQuery(request);
        queryBuilder.withQuery(basicQuery);
        //2.2减少查询条件
        //每页显示一个
        queryBuilder.withPageable(PageRequest.of(0, 1));
        //显示空的source
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());
        //3.聚合条件
        //3.1分类聚合
        String categoryAgg = "categoryAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(categoryAgg).field("categoryId"));
        //3.2聚合品牌
        String brandAgg = "brandAgg";
        queryBuilder.addAggregation(AggregationBuilders.terms(brandAgg).field("brandId"));

        //4.查询结果并解析
        AggregatedPage<Goods> result = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = result.getAggregations();

        //获取category的聚合
        LongTerms cTerms = aggregations.get(categoryAgg);
        List<Long> ids = handleCategoryAgg(cTerms, filterList);


        //获取brand的聚合
        LongTerms bTerms = aggregations.get(brandAgg);
        handleBrandAgg(bTerms, filterList);

        //规格参数的处理
        if (ids != null && ids.size() == 1) {
            //处理规格参数的聚合函数
            handleSpecAgg(ids.get(0), basicQuery, filterList);
        }

        return filterList;
    }

    /**
     * 对规格参数的解析
     * @param cid
     * @param basicQuery
     * @param filterList
     */
    private void handleSpecAgg(Long cid, QueryBuilder basicQuery, Map<String, List<?>> filterList) {
        //1.查询分类下的所有规格参数
        List<SpecParamDTO> specParams = itemClient.querySpecParams(null, cid, true);
        //2.构建查询条件
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder();
        queryBuilder.withQuery(basicQuery);
        //2.2减少查询的结果
        queryBuilder.withPageable(PageRequest.of(0, 1));
        //2.3显示空的source
        queryBuilder.withSourceFilter(new FetchSourceFilterBuilder().build());
        //3.聚合条件
        for (SpecParamDTO specParam : specParams) {
            //获取规格参数的名称作为
            String name = specParam.getName();
            queryBuilder.addAggregation(AggregationBuilders.terms(name).field("specs." + name));
        }
        //4.进行查询
        AggregatedPage<Goods> result = esTemplate.queryForPage(queryBuilder.build(), Goods.class);
        Aggregations aggregations = result.getAggregations();

        //5.对结果进行解析
        for (SpecParamDTO specParam : specParams) {
            //获取参数的name 作为聚合名字
            String name = specParam.getName();
            //根据名字获取局和结果
            Terms terms = aggregations.get(name);
            //对聚合结果进行解析  转变成一个String 的集合
            List<String> paramValues = terms.getBuckets().stream()
                    .map(Terms.Bucket::getKeyAsString)
                    .filter(StringUtils::isNotBlank)
                    .collect(Collectors.toList());
            //将规格参数的名字  和  对应的值 存入到Map中
            filterList.put(name, paramValues);
        }


    }

    /**
     * 处理分类的聚合结果
     * @param cTerms
     * @param map
     */
    private List<Long> handleCategoryAgg(LongTerms cTerms, Map<String, List<?>> map) {
        //解析bucket，得到id集合
        List<Long> ids = cTerms.getBuckets().stream().map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue).collect(Collectors.toList());
//        Stream<Number> numberStream = cTerms.getBuckets().stream().map(LongTerms.Bucket::getKeyAsNumber);
//        List<Long> ids = numberStream.map(Number::longValue).collect(Collectors.toList());
        //根据id查询categoryList
        List<CategoryDTO> list = itemClient.queryCategoryByIds(ids);

        map.put("分类", list);

        return ids;
    }

    /**
     * 对品牌的聚合解析
     * @param bTerms
     * @param map
     */
    private void handleBrandAgg(LongTerms bTerms, Map<String, List<?>> map) {
        //解析bucket，得到brand的ids
        List<Long> ids = bTerms.getBuckets().stream().map(LongTerms.Bucket::getKeyAsNumber)
                .map(Number::longValue).collect(Collectors.toList());

        //根据ids 查询brandList
        List<BrandDTO> list = itemClient.queryBrandsByIds(ids);

        map.put("品牌", list);

    }

    /**
     * 根据spuID 添加索引库数据
     * @param id
     */
    public void creatIndex(Long id) {
        //查询spu
        SpuDTO spuDTO = itemClient.querySpuById(id);
        //转换为goods 因为只有goods 才是和索引库打交道的
        Goods goods = buildGoods(spuDTO);
        //保存数据到索引库
        goodsRepository.save(goods);

    }

    /**
     * 根据spuID 删除对应的索引库数据
     * @param id
     */
    public void deleteIndex(Long id) {
        goodsRepository.deleteById(id);
    }

}
