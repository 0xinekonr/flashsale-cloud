package com.axin.flashsale.order.listener;

import com.axin.flashsale.order.config.RabbitConfig;
import com.axin.flashsale.order.entity.Order;
import com.axin.flashsale.order.mapper.OrderMapper;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class OderListener {

    @Autowired
    private OrderMapper orderMapper;

    @RabbitListener(queues = RabbitConfig.ORDER_PAY_QUEUE)
    public void handlePaySuccess(Long orderId) {
        System.out.println("收到支付成功消息，订单ID：" + orderId);

        // 1. 查询订单
        Order order = orderMapper.selectById(orderId);

        // 2. 幂等性判断（防止重复消费）
        if (order == null || !"NEW".equals(order.getStatus())) {
            System.out.println("订单不存在或已处理，跳过。Status=" + (order == null ? "null" : order.getStatus()));
            return;
        }

        // 3. 修改状态
        order.setStatus("PAID");
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        System.out.println("订单 " + orderId + " 状态已更新为 PAID");
    }
}
