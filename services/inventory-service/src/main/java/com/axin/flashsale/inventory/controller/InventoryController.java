package com.axin.flashsale.inventory.controller;

import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.inventory.entity.Inventory;
import com.axin.flashsale.inventory.mapper.InventoryMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/inventory")
public class InventoryController {

    @Autowired
    private InventoryMapper inventoryMapper;

    @PostMapping("/{productId}/lock")
    public Boolean lockStock(@PathVariable Long productId, @RequestParam Integer count) {
        return inventoryMapper.lockStock(productId, count) > 0;
    }

    @GetMapping("/{productId}")
    public Result<Inventory> getStock(@PathVariable Long productId) {
        Inventory inventory = inventoryMapper.selectOne(
                new QueryWrapper<Inventory>().eq("product_id", productId));
        return Result.success(inventory);
    }
}
