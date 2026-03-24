package com.axin.flashsale.order.listener;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.dto.OrderCancelEventDTO;
import com.axin.flashsale.order.entity.Order;
import com.axin.flashsale.order.enums.OrderStatusEnum;
import com.axin.flashsale.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
public class OrderTimeoutCancelListener {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RabbitListener(queues = GlobalConstants.MQ.ORDER_DEAD_LETTER_QUEUE)
    @Transactional(rollbackFor = Exception.class)
    public void handleOrderTimeout(Long orderId) {
        log.info("接收到订单超时检测消息，订单ID: {}", orderId);

        // 1. 查询订单当前状态
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return;
        }

        // 2. 判断是否依然是"待支付"状态
        if (OrderStatusEnum.NEW.getCode().equals(order.getStatus())) {
            log.warn("订单 [{}] 超时未支付，执行自动取消逻辑！", orderId);
            // 3. 修改状态为已取消
            order.setStatus(OrderStatusEnum.CANCELLED.getCode());
            orderMapper.updateById(order);

            // 4. 发送"订单已取消"广播事件，通知其他微服务回补资源
            OrderCancelEventDTO event = OrderCancelEventDTO.builder()
                    .orderId(orderId)
                    .userId(order.getUserId())
                    .productId(order.getProductId())
                    .count(order.getCount())
                    .build();
            log.info("订单 [{}] 状态已置为取消，准备回补广播...", orderId);
            rabbitTemplate.convertAndSend(GlobalConstants.MQ.ORDER_EVENT_EXCHANGE,
                    GlobalConstants.MQ.ORDER_CANCEL_NOTIFY_ROUTING_KEY,
                    event);

        } else {
            log.warn("订单 [{}] 状态为 {}，无需取消！", orderId, order.getStatus());
        }
    }
}