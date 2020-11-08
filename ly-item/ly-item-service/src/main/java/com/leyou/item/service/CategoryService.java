package com.leyou.item.service;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.utils.JsonUtils;
import com.leyou.item.dto.CategoryDTO;
import com.leyou.item.entity.Category;
import com.leyou.item.mapper.CategoryMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryService {
    @Autowired
    private CategoryMapper categoryMapper;


    public List<CategoryDTO> queryByParentId(Long pid) {
        //查询条件 ，mapper会把对象中的非空属性作为查询条件
        Category c = new Category();
        c.setParentId(pid);
        List<Category> list = categoryMapper.select(c);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, CategoryDTO.class);
    }

    public List<CategoryDTO> queryByBrandId(Long brandId) {

        List<Category> list = categoryMapper.queryByBrandId(brandId);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, CategoryDTO.class);
    }

    /**
     * 根据多级路径查询商品的分类
     * @param ids
     * @return
     */
    public List<CategoryDTO> queryCategoryByIds(List<Long> ids) {
        List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, CategoryDTO.class);

    }

    /**
     * 根据ids 查询分类集合
     * @param ids
     * @return
     */
    public List<CategoryDTO> queryByIds(List<Long> ids) {
        List<Category> list = categoryMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, CategoryDTO.class);
    }


    /**
     * 根据3级分类id，查询1~3级的分类
     * @param cid3
     * @return
     */
    public List<CategoryDTO> queryAllByCid3(Long cid3) {
        Category c3 = categoryMapper.selectByPrimaryKey(cid3);
        if (c3 == null) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        Category c2 = categoryMapper.selectByPrimaryKey(c3.getParentId());
        if (c2 == null) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        Category c1 = categoryMapper.selectByPrimaryKey(c2.getParentId());
        if (c1 == null ) {
            throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
        }
        List<Category> list = Arrays.asList(c1, c2, c3);
        return BeanHelper.copyWithCollection(list, CategoryDTO.class);
    }

    @Autowired
    private StringRedisTemplate redisTemplate;

    private static final String PRE_FIX = "category:list:";
    /**
     * 查询一级分类
     * @return
     */
    public List<CategoryDTO> queryOneList() {
        List<CategoryDTO> oneList = new ArrayList<>();
        //先从redis 中拿
        String key = PRE_FIX + "oneList";
        String oneListJson = redisTemplate.opsForValue().get(key);

//        List<CategoryDTO> oneList = JsonUtils.toList(oneListJson, CategoryDTO.class);
        if (StringUtils.isBlank(oneListJson)) {
            //如果为空，那么查询数据库 并把数据保存到redis中
            Category category = new Category();
            category.setParentId(0L);
            oneList = BeanHelper.copyWithCollection(categoryMapper.select(category),CategoryDTO.class);
            if (CollectionUtils.isEmpty(oneList)) {
                throw new LyException(ExceptionEnum.CATEGORY_NOT_FOUND);
            }
            //保存到redis 中
            redisTemplate.opsForValue().set(key, JsonUtils.toString(oneList));
            return oneList;
        }
        return JsonUtils.toList(oneListJson, CategoryDTO.class);


    }

    /**
     * 根据一级id 查询2 ，
     *
     * @return
     */
    public  List<Map<String, List<CategoryDTO>>> twoAndThreeList(Long id) {

        //根据一级id查询 对应的2级目录
        Category category = new Category();
        category.setParentId(id);
        List<Category> twoList = categoryMapper.select(category);
        HashMap<String, List<CategoryDTO>> map = new HashMap<>();
        List<Map<String,List<CategoryDTO>>> dataList = new ArrayList<>();
        for (Category c : twoList) {
            category.setParentId(c.getId());
            List<Category> threeList = categoryMapper.select(category);
            List<CategoryDTO> list = BeanHelper.copyWithCollection(threeList, CategoryDTO.class);
            map.put(c.getName(), list);
            dataList.add(map);
        }
        return dataList;

    }
}
