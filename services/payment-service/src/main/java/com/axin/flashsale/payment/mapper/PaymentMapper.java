package com.axin.flashsale.payment.mapper;

import com.axin.flashsale.payment.entity.Payment;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PaymentMapper extends BaseMapper<Payment> {

    @Select("SELECT * FROM payment WHERE status = 1 AND update_time > DATE_SUB(NOW(), INTERVAL #{hours} HOUR)")
    List<Payment> findRecentSuccessPayments(@Param("hours") int hours);
}
