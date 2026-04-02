package com.axin.flashsale.order.component;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.dto.OrderCancelEventDTO;
import com.axin.flashsale.common.redis.lock.DistributedLock;
import com.axin.flashsale.order.entity.Order;
import com.axin.flashsale.order.enums.OrderStatusEnum;
import com.axin.flashsale.order.mapper.OrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 订单超时扫描器
 * 定时扫描超时未支付的订单并执行取消逻辑
 * 作为 MQ 延迟队列的兜底机制
 */
@Slf4j
@Component
public class OrderTimeoutScanner {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private DistributedLock distributedLock;

    private static final int TIMEOUT_MINUTES = 20;
    private static final String LOCK_KEY = "timeout:scanner";

    /**
     * 每分钟扫描一次超时订单
     */
    @Scheduled(fixedRate = 60000)
    public void scanTimeoutOrders() {
        distributedLock.tryLock(LOCK_KEY, 0, 30, TimeUnit.SECONDS, () -> {
            doScan();
        });
    }

    private void doScan() {
        List<Order> timeoutOrders = orderMapper.findTimeoutOrders(TIMEOUT_MINUTES);
        if (timeoutOrders.isEmpty()) {
            return;
        }

        log.info("扫描到 {} 个超时未支付订单", timeoutOrders.size());

        for (Order order : timeoutOrders) {
            try {
                cancelOrder(order);
            } catch (Exception e) {
                log.error("取消订单失败, orderId={}", order.getId(), e);
            }
        }
    }

    private void cancelOrder(Order order) {
        // 再次检查状态（防止与 MQ 消费者并发冲突）
        Order latest = orderMapper.selectById(order.getId());
        if (latest == null || !OrderStatusEnum.NEW.getCode().equals(latest.getStatus())) {
            log.debug("订单状态已变更, orderId={}, status={}", order.getId(),
                    latest != null ? latest.getStatus() : "null");
            return;
        }

        // 更新状态为已取消
        latest.setStatus(OrderStatusEnum.CANCELLED.getCode());
        orderMapper.updateById(latest);

        // 发送订单取消事件
        OrderCancelEventDTO event = OrderCancelEventDTO.builder()
                .orderId(order.getId())
                .userId(order.getUserId())
                .productId(order.getProductId())
                .count(order.getCount())
                .build();

        rabbitTemplate.convertAndSend(
                GlobalConstants.MQ.ORDER_EVENT_EXCHANGE,
                GlobalConstants.MQ.ORDER_CANCEL_NOTIFY_ROUTING_KEY,
                event
        );

        log.info("订单超时取消成功, orderId={}", order.getId());
    }
}