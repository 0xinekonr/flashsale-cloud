package com.axin.flashsale.order.listener;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.dto.SeckillMessage;
import com.axin.flashsale.order.client.InventoryClient;
import com.axin.flashsale.order.entity.Order;
import com.axin.flashsale.order.enums.OrderStatusEnum;
import com.axin.flashsale.order.mapper.OrderMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
public class SeckillOrderListener {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private InventoryClient inventoryClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Transactional(rollbackFor = Exception.class)
    @RabbitListener(queues = GlobalConstants.MQ.SECKILL_ORDER_QUEUE)
    public void createSeckillOrder(SeckillMessage message) {
        // 幂等检查：同一用户+同一活动只能有一单
        Long existCount = orderMapper.selectCount(
                new QueryWrapper<Order>()
                        .eq("user_id", message.getUserId())
                        .eq("product_id", message.getProductId())
                        .ne("status", OrderStatusEnum.CANCELLED.getCode()));
        if (existCount > 0) {
            log.warn("秒杀订单已存在, userId={}, productId={}, 跳过创建",
                    message.getUserId(), message.getProductId());
            return;
        }

        // 1. 组装并插入订单
        Order order = new Order();
        order.setUserId(message.getUserId());
        order.setProductId(message.getProductId());
        order.setCount(1);
        order.setTotalAmount(message.getSeckillPrice());
        order.setStatus(OrderStatusEnum.NEW.getCode());
        order.setCreateTime(LocalDateTime.now());

        orderMapper.insert(order);
        log.info("秒杀订单已插入, orderId={}", order.getId());

        // 2. 远程锁定库存
        Boolean lockResult = inventoryClient.lockStock(message.getProductId(), 1);
        log.info("库存锁定结果: {}", lockResult);

        // 3. 发送订单ID到延迟队列
        rabbitTemplate.convertAndSend(GlobalConstants.MQ.ORDER_EVENT_EXCHANGE,
                GlobalConstants.MQ.ORDER_CREATE_ROUTING_KEY,
                order.getId());
        log.info("秒杀订单[{}]创建完成, 已发送延迟取消检测消息", order.getId());
    }
}
