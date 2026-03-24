package com.axin.flashsale.order.controller;

import com.axin.flashsale.common.dto.ProductSkuDTO;
import com.axin.flashsale.common.exception.BizException;
import com.axin.flashsale.common.exception.SystemCode;
import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.order.client.InventoryClient;
import com.axin.flashsale.order.client.ProductClient;
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

    @Autowired
    private ProductClient productClient;

    @PostMapping
    @Transactional
    public Result<Long> createOrder(@RequestBody Order order) {
        // 1. 从 product-service 获取价格
        ProductSkuDTO sku = productClient.getSku(order.getProductId(), order.getSkuId());
        if (sku == null) {
            throw new BizException(SystemCode.NOT_FOUND.getCode(), "商品不存在");
        }
        BigDecimal price = sku.getPrice();

        // 2. 远程锁定库存
        Boolean lockSuccess = inventoryClient.lockStock(order.getProductId(), order.getCount());
        if (!lockSuccess) {
            throw new BizException(SystemCode.SYSTEM_ERROR.getCode(), "下单失败：库存不足");
        }

        // 3. 库存锁定成功，创建订单
        order.setCreateTime(LocalDateTime.now());
        order.setStatus(OrderStatusEnum.NEW.getCode());
        order.setTotalAmount(price.multiply(new BigDecimal(order.getCount())));

        orderMapper.insert(order);
        log.info("订单创建成功, orderId={}, productId={}, price={}", order.getId(), order.getProductId(), price);
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
