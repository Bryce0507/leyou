package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.*;
import com.leyou.item.entity.Sku;
import com.leyou.item.entity.Spu;
import com.leyou.item.entity.SpuDetail;
import com.leyou.item.mapper.SkuMapper;
import com.leyou.item.mapper.SpuDetailMapper;
import com.leyou.item.mapper.SpuMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.el.stream.Stream;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_DOWN_KEY;
import static com.leyou.common.constants.MQConstants.RoutingKey.ITEM_UP_KEY;

@Service
public class GoodsService {
    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private CategoryService categoryService;
    @Autowired
    private BrandService brandService;
    @Autowired
    private SpuDetailMapper spuDetailMapper;
    @Autowired
    private SkuMapper skuMapper;
    @Autowired
    private AmqpTemplate amqpTemplate;

    public PageResult<SpuDTO> querySpuPage(String key, Boolean saleable, Integer page, Integer rows) {
        //分页
        PageHelper.startPage(page, rows);
        //过滤
        Example example = new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        //搜索条件
        if (StringUtils.isNoneBlank(key)) {
            criteria.andLike("name", "%" + key + "%");
        }
        //上下架过滤
        if (saleable != null) {
            criteria.andEqualTo("saleable", saleable);
        }
        //默认按照时间顺序查询
        example.setOrderByClause("update_time DESC");

        //查询结果
        List<Spu> list = spuMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        //分装结果
        PageInfo<Spu> info = new PageInfo<>(list);

        List<SpuDTO> dtoList = BeanHelper.copyWithCollection(list, SpuDTO.class);
        //处理显示的category和brand名字问题

        handleCategoryAndBrandName(dtoList);

        return new PageResult<SpuDTO>(info.getTotal(), dtoList);

    }

    /**
     * 处理商品显示的category 和brand 名字问题
     * @param list
     */
    private void handleCategoryAndBrandName(List<SpuDTO> list) {
        for (SpuDTO spuDTO : list) {
            String categoryName = categoryService.queryCategoryByIds(spuDTO.getCategoryIds())
                    .stream()
                    .map(CategoryDTO::getName).collect(Collectors.joining("/"));
            spuDTO.setCategoryName(categoryName);
            //查询品牌
            BrandDTO brandDTO = brandService.queryById(spuDTO.getBrandId());
            spuDTO.setBrandName(brandDTO.getName());
        }
    }


    /**
     * 新增商品
     *
     * @param spuDTO
     */
    @Transactional
    public void saveGoods(SpuDTO spuDTO) {
        //从dto中取出spu信息
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        //保存spu
        spu.setId(null);
        spu.setCreateTime(null);
        spu.setUpdateTime(null);
        int count = spuMapper.insertSelective(spu);
        if (count !=1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
        spuDetail.setSpuId(spu.getId());
        count = spuDetailMapper.insertSelective(spuDetail);
        if (count !=1) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

        List<SkuDTO> skuDTOList = spuDTO.getSkus();
        List<Sku> skuList = new ArrayList<>();
        for (SkuDTO skuDTO : skuDTOList) {
            skuDTO.setSpuId(spu.getId());
            skuList.add(BeanHelper.copyProperties(skuDTO, Sku.class));
        }
         count = skuMapper.insertList(skuList);

        if (count != skuList.size()) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }

    }

    /**
     * 上下架业务
     * @param id
     * @param saleable
     */
    @Transactional
    public void updateSpuSaleable(Long id, Boolean saleable) {

        Spu spu = new Spu();
        spu.setId(id);
        spu.setSaleable(saleable);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //商品下架 对应的sku也要变成unable状态

        Example example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId", id);
        Sku sku = new Sku();
        sku.setEnable(saleable);
        skuMapper.updateByExampleSelective(sku, example);

        //发送mq消息
        String key = saleable ? ITEM_UP_KEY : ITEM_DOWN_KEY;

        /**
         * 参数1：类似 接收的地址    参数2  message
         */
        amqpTemplate.convertAndSend(key, id);

    }

    /**
     * 数据回显 根据id 查spuDetail
     * @param id
     * @return
     */
    public SpuDetailDTO querySpuDetailById(Long id) {
        SpuDetail spuDetail = spuDetailMapper.selectByPrimaryKey(id);
        if (spuDetail == null) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyProperties(spuDetail, SpuDetailDTO.class);
    }

    public List<SkuDTO> querySkusBySpuId(Long id) {
        Sku sku = new Sku();
        sku.setSpuId(id);
        List<Sku> skuList = skuMapper.select(sku);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(skuList, SkuDTO.class);
    }

    /**
     * 商品修改的按钮
     * @param spuDTO
     */

    @Transactional
    public void updateGoods(SpuDTO spuDTO) {
        //修改spu表的数据
        Spu spu = BeanHelper.copyProperties(spuDTO, Spu.class);
        spu.setSaleable(null);
        spu.setUpdateTime(null);
        int count = spuMapper.updateByPrimaryKeySelective(spu);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //修改spuDetail 表的数据
        SpuDetailDTO spuDetailDTO = spuDTO.getSpuDetail();
        SpuDetail spuDetail = BeanHelper.copyProperties(spuDetailDTO, SpuDetail.class);
        count = spuDetailMapper.updateByPrimaryKeySelective(spuDetail);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }

        //sku是一个list 只知道spuId并不能知道修改哪个sku
        //所以需要先删后增
        Sku sku = new Sku();
        sku.setSpuId(spu.getId());
        skuMapper.delete(sku);

        //后增
        List<SkuDTO> skuDTOList = spuDTO.getSkus();
        List<Sku> skuList = BeanHelper.copyWithCollection(skuDTOList, Sku.class);
        for (Sku sku1 : skuList) {
            sku1.setSpuId(spu.getId());
        }
        //插入数据库
        count = skuMapper.insertList(skuList);
        if (count != skuList.size()) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }


    }

    @Transactional
    public void deleteSpuById(Long id) {
        //先删除spu
        int count = spuMapper.deleteByPrimaryKey(id);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }

        //删除spuDetail
        count = spuDetailMapper.deleteByPrimaryKey(id);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
        //删除sku
        Sku sku = new Sku();
        sku.setSpuId(id);
        count = skuMapper.delete(sku);
    }

    /**
     * 根据id查询spuDTO
     * @param id
     * @return
     */
    public SpuDTO querySpuById(Long id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        SpuDTO spuDTO = BeanHelper.copyProperties(spu, SpuDTO.class);
        //查询spuDetail
        spuDTO.setSpuDetail(querySpuDetailById(id));
        //查询sku
        spuDTO.setSkus(querySkusBySpuId(id));

        return spuDTO;


    }

    /**
     * 根据ids 查询skuDTO List
     * @param ids
     * @return
     */
    public List<SkuDTO> querySkuByIds(List<Long> ids) {
//        List<Long> list = ids.stream().map(Long::valueOf).collect(Collectors.toList());
        List<Sku> skuList = skuMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(skuList)) {
            throw new LyException(ExceptionEnum.GOODS_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(skuList, SkuDTO.class);
    }

    /**
     * 减库存
     * @param cartMap
     */
    @Transactional
    public void minusStock(Map<Long, Integer> cartMap) {
        for (Map.Entry<Long, Integer> entry : cartMap.entrySet()) {
            Long skuId = entry.getKey();
            Integer num = entry.getValue();
            int count = skuMapper.minusStock(skuId, num);
            if (count != 1) {
                throw new RuntimeException("库存不足！");
            }
        }

    }
}
