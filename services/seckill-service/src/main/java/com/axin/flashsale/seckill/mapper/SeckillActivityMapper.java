package com.axin.flashsale.seckill.mapper;

import com.axin.flashsale.seckill.entity.SeckillActivity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivity> {

    // 扣减活动库存
    @Update("UPDATE seckill_activity SET available_stock = available_stock - 1 WHERE id = #{id} AND available_stock > 0")
    int deductStock(@Param("id") Long id);
}
