package com.axin.flashsale.order.listener;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.order.entity.Order;
import com.axin.flashsale.order.enums.OrderStatusEnum;
import com.axin.flashsale.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
public class OrderPaymentListener {

    @Autowired
    private OrderMapper orderMapper;

    @RabbitListener(queues = GlobalConstants.MQ.ORDER_PAY_QUEUE)
    public void handlePaySuccess(Long orderId) {
        log.info("收到支付成功消息, orderId={}", orderId);

        Order order = orderMapper.selectById(orderId);

        if (order == null || !OrderStatusEnum.NEW.getCode().equals(order.getStatus())) {
            log.warn("订单不存在或已处理, orderId={}, status={}",
                    orderId, order == null ? "null" : order.getStatus());
            return;
        }

        order.setStatus(OrderStatusEnum.PAID.getCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单[{}]状态已更新为 PAID", orderId);
    }
}
