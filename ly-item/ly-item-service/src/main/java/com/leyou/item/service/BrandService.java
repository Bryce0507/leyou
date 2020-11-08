package com.leyou.item.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exceptions.LyException;
import com.leyou.common.utils.BeanHelper;
import com.leyou.common.vo.PageResult;
import com.leyou.item.dto.BrandDTO;
import com.leyou.item.entity.Brand;
import com.leyou.item.mapper.BrandMapper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class BrandService {
    @Autowired
    private BrandMapper brandMapper;


    public PageResult<BrandDTO> queryBrandByPage(Integer page, Integer rows, String sortBy, String key, Boolean desc) {
        //通用mapper里面的 开启分页
        PageHelper.startPage(page, rows);
        //过滤条件
        Example example = new Example(Brand.class);
        //判断key是否为空
        if (StringUtils.isNoneBlank(key)) {
            example.createCriteria().orLike("name", "%" + key + "%")
                        .orEqualTo("letter", key.toUpperCase());
        }
        //排序
        if (StringUtils.isNoneBlank(sortBy)) {
            String orderByClause = sortBy + (desc ? " DESC" : " ASC");
            example.setOrderByClause(orderByClause);
        }
        //查询
        List<Brand> brands = brandMapper.selectByExample(example);
        //判断查询的数据是否为空
        if (CollectionUtils.isEmpty(brands)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        //解析分页结果
        PageInfo<Brand> pageInfo = new PageInfo<>(brands);
        //转换为BrandDTO
        List<BrandDTO> list = BeanHelper.copyWithCollection(brands, BrandDTO.class);

        return new PageResult<>(pageInfo.getTotal(), list);
    }

    /**
     * 新增品牌
     * @param brandDTO
     * @param ids
     */
    @Transactional
    public void saveBrand(BrandDTO brandDTO, List<Long> ids) {
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        brand.setId(null);
        int count = brandMapper.insertSelective(brand);
        if (count == 0) {
            //新增失败
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
        //新增品牌和分类的中间表
        count = brandMapper.insertCategoryBrand(brand.getId(), ids);
        if (count !=ids.size()) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 修改品牌
     * @param brandDTO
     * @param ids
     */
    @Transactional
    public void updateBrand(BrandDTO brandDTO, List<Long> ids) {
        Brand brand = BeanHelper.copyProperties(brandDTO, Brand.class);
        //修改品牌
        int count = brandMapper.updateByPrimaryKeySelective(brand);
        if (count != 1) {
            throw new LyException(ExceptionEnum.UPDATE_OPERATION_FAIL);
        }
        //删除中间表数据
        brandMapper.deleteCategoryBrand(brand.getId());
        //重新插入中间表数据
        count = brandMapper.insertCategoryBrand(brand.getId(), ids);
        if (count != ids.size()) {
            throw new LyException(ExceptionEnum.INSERT_OPERATION_FAIL);
        }
    }

    /**
     * 删除按钮
     * @param bid
     */
    public void deleteBrand(Long bid) {
        int count = brandMapper.deleteByPrimaryKey(bid);
        if (count != 1) {
            throw new LyException(ExceptionEnum.DELETE_OPERATION_FAIL);
        }
        brandMapper.deleteCategoryBrand(bid);
    }

    /**
     * 根据id查询BrandDTO
     * @param bid
     * @return
     */
    public BrandDTO queryById(Long bid) {
        Brand brand = brandMapper.selectByPrimaryKey(bid);
        if (brand == null) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyProperties(brand, BrandDTO.class);

    }

    /**
     * 根据cid查询brandDTO的list
     * @param cid
     * @return
     */
    public List<BrandDTO> queryBrandByCid(Long cid) {
        List<Brand> list = brandMapper.queryBrandByCid(cid);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, BrandDTO.class);
    }

    /**
     * 根据ids 查询brandDTO对象的集合
     * @param ids
     * @return
     */
    public List<BrandDTO> queryBrandsByIds(List<Long> ids) {
        List<Brand> list = brandMapper.selectByIdList(ids);
        if (CollectionUtils.isEmpty(list)) {
            throw new LyException(ExceptionEnum.BRAND_NOT_FOUND);
        }
        return BeanHelper.copyWithCollection(list, BrandDTO.class);
    }
}
