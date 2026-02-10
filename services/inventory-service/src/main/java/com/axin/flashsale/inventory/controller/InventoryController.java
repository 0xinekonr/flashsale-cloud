package com.axin.flashsale.inventory.controller;

import com.axin.flashsale.inventory.entity.Inventory;
import com.axin.flashsale.inventory.mapper.InventoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryMapper inventoryMapper;

    /**
     * 锁定库存接口
     * 返回 true 表示锁定成功， false 表示库存不足
     */
    @PostMapping("/{productId}/lock")
    public Boolean lockStock(@PathVariable Long productId, @RequestParam Integer count) {
        return inventoryMapper.lockStock(productId, count) > 0;
    }

    @GetMapping("/{productId}")
    public Object getStock(@PathVariable Long productId) {
        // 使用 QueryWrapper 查询某商品库存
        return inventoryMapper.selectList(new QueryWrapper<Inventory>().eq("product_id", productId));
    }
}
