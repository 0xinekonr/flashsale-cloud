package com.axin.flashsale.inventory.mapper;

import com.axin.flashsale.inventory.entity.Inventory;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface InventoryMapper extends BaseMapper<Inventory> {

    /**
     * 核心 SQL：扣减可用库存，增加锁定库存
     * 只有当 available_stock >= count 才执行扣减
     */
    @Update("UPDATE inventory SET available_stock = available_stock - #{count}, locked_stock = locked_stock + #{count}" +
            " WHERE product_id = #{productId} AND available_stock >= #{count}")
    int lockStock(@Param("productId") Long productId, @Param("count") Integer count);
}
