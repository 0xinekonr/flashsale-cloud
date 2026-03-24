package com.axin.flashsale.order.controller;

import com.axin.flashsale.common.exception.BizException;
import com.axin.flashsale.common.exception.SystemCode;
import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.order.client.InventoryClient;
import com.axin.flashsale.order.entity.Order;
import com.axin.flashsale.order.enums.OrderStatusEnum;
import com.axin.flashsale.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private InventoryClient inventoryClient;

    @PostMapping
    @Transactional
    public Result<Long> createOrder(@RequestBody Order order) {
        // 1. 远程锁定库存
        Boolean lockSuccess = inventoryClient.lockStock(order.getProductId(), order.getCount());
        if (!lockSuccess) {
            throw new BizException(SystemCode.SYSTEM_ERROR.getCode(), "下单失败：库存不足");
        }

        // 2. 库存锁定成功，创建订单
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(OrderStatusEnum.NEW.getCode());
        // 价格暂用硬编码（M1 接入 product-service 后替换）
        order.setTotalAmount(new BigDecimal("100").multiply(new BigDecimal(order.getCount())));

        orderMapper.insert(order);
        log.info("订单创建成功, orderId={}", order.getId());
        return Result.success(order.getId());
    }

    @GetMapping("/{id}")
    public Result<Order> getOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        return Result.success(order);
    }

    @PostMapping("/{id}/finish")
    public Result<Boolean> finishOrder(@PathVariable Long id) {
        Order order = orderMapper.selectById(id);
        if (order != null && OrderStatusEnum.NEW.getCode().equals(order.getStatus())) {
            order.setStatus(OrderStatusEnum.PAID.getCode());
            orderMapper.updateById(order);
            return Result.success(true);
        }
        return Result.success(false);
    }
}
