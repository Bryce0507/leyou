package com.leyou.item.mapper;

import com.leyou.item.entity.Category;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;
import tk.mybatis.mapper.additional.idlist.IdListMapper;
import tk.mybatis.mapper.common.IdsMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

@Repository
public interface CategoryMapper extends Mapper<Category>, IdsMapper<Category>, IdListMapper<Category, Long> {
    @Select("SELECT\n" +
            "\ttc.id,\n" +
            "\ttc.name,\n" +
            "\ttc.is_parent,\n" +
            "\ttc.sort\n" +
            "FROM\n" +
            "\ttb_category_brand tcb\n" +
            "LEFT JOIN tb_category tc ON tcb.category_id = tc.id\n" +
            "WHERE tcb.brand_id = #{brandId}")
    List<Category> queryByBrandId(@Param("brandId") Long brandId);
}
