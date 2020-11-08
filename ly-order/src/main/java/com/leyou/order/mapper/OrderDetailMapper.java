package com.leyou.order.mapper;

import com.leyou.common.mapper.BaseMapper;
import com.leyou.order.entity.OrderDetail;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailMapper extends BaseMapper<OrderDetail>{

    /**
     * 批量新增orderDetails
     * @param details
     * @return
     */
    int insertDetailList(@Param("details") List<OrderDetail> details);
}