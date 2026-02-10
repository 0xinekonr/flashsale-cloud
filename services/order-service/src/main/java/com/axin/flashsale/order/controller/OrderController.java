package com.axin.flashsale.order.controller;

import com.axin.flashsale.order.client.InventoryClient;
import com.axin.flashsale.order.entity.Order;
import com.axin.flashsale.order.mapper.OrderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private InventoryClient inventoryClient;    // 注入 Feign 客户端


    @PostMapping
    @Transactional  // 本地事务：保证订单入库失败回滚
    public String createOrder(@RequestBody Order order){

        // 1. 远程锁定库存 （RPC)
        // 注意：这里没有分布式事务（Seata），如果锁库存成功但单面报错，库存不会回滚
        Boolean lockSuccess = inventoryClient.lockStock(order.getProductId(), order.getCount());
        if(!lockSuccess){
            return "下单失败：库存不足";
        }

        // 2. 库存锁定成功，创建订单
        order.setCreateTime(LocalDateTime.now());
        order.setStatus("NEW");
        order.setTotalAmount(new BigDecimal("100").multiply(new BigDecimal(order.getCount())));

        orderMapper.insert(order);
        return "下单成功，订单号：" + order.getId();
    }

    @GetMapping("/{id}")
    public Object getOrder(@PathVariable Long id) {
        return orderMapper.selectById(id);
    }

    /**
     * 供 Payment Service 回调，将订单状态改为 PAID
     */
    @PostMapping("/{id}/finish")
    public Boolean finishOrder(@PathVariable Long id){
        Order order = orderMapper.selectById(id);
        if (order != null && "NEW".equals(order.getStatus())) {
            order.setStatus("PAID");
            orderMapper.updateById(order);
            return true;
        }
        return false;
    }

}
