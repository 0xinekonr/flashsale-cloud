package com.axin.flashsale.order.mapper;

import com.axin.flashsale.order.entity.Order;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("SELECT * FROM orders WHERE status = 0 AND create_time < DATE_SUB(NOW(), INTERVAL #{minutes} MINUTE)")
    List<Order> findTimeoutOrders(@Param("minutes") int minutes);
}
