package com.axin.flashsale.seckill.mapper;

import com.axin.flashsale.seckill.entity.SeckillActivity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SeckillActivityMapper extends BaseMapper<SeckillActivity> {

    // 扣减活动库存（每次扣 1，用于秒杀实时扣减）
    @Update("UPDATE seckill_activity SET available_stock = available_stock - 1 WHERE id = #{id} AND available_stock > 0")
    int deductStock(@Param("id") Long id);

    // 对账用：直接设置库存值（以 Redis 为准修复 DB 漂移）
    @Update("UPDATE seckill_activity SET available_stock = #{stock} WHERE id = #{id}")
    int setStock(@Param("id") Long id, @Param("stock") int stock);
}
