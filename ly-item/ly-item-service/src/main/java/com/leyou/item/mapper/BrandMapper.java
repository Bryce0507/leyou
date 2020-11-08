package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.entity.Brand;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface BrandMapper extends BaseMapper<Brand> {
    /**
     * 新增品牌，往品牌和类型的中间表中插数据
     * @param bid
     * @param ids
     * @return
     */
    int insertCategoryBrand(@Param("bid") Long bid,@Param("ids") List<Long> ids);

    @Delete("delete from tb_category_brand where brand_id = #{bid}")
    int deleteCategoryBrand(@Param("bid") Long id);

    @Select("select * from tb_brand where id in (select brand_id from tb_category_brand where category_id = #{cid})")
    List<Brand> queryBrandByCid(@Param("cid") Long cid);
}
