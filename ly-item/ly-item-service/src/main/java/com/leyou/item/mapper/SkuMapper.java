package com.leyou.item.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.item.entity.Sku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuMapper extends BaseMapper<Sku> {

    /**
     * 减少库存
     * @param skuId
     * @param num
     * @return
     */
    @Update("update tb_sku set stock=stock - #{num} where id = #{skuId}")
    int minusStock(@Param("skuId") Long skuId,@Param("num") Integer num);
}
