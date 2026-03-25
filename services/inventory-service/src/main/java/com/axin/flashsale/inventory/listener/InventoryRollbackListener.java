package com.axin.flashsale.inventory.listener;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.dto.OrderCancelEventDTO;
import com.axin.flashsale.common.mq.consumer.IdempotentConsumer;
import com.axin.flashsale.inventory.mapper.InventoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

@Slf4j
@Component
public class InventoryRollbackListener {

    @Autowired
    private InventoryMapper inventoryMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    private IdempotentConsumer idempotentConsumer;

    @PostConstruct
    public void init() {
        this.idempotentConsumer = new IdempotentConsumer(
                redisTemplate,
                "inventory:consumed:",
                24 // TTL: 24 hours
        );
    }

    @RabbitListener(queues = GlobalConstants.MQ.INVENTORY_ROLLBACK_QUEUE)
    public void handleStockRollback(OrderCancelEventDTO event) {
        log.info("收到库存回滚事件, orderId={}, productId={}, count={}",
                event.getOrderId(), event.getProductId(), event.getCount());

        // 使用 orderId 作为幂等键，确保同一订单只回滚一次
        String messageId = "rollback:" + event.getOrderId();

        idempotentConsumer.consume(messageId, event, this::doRollback);
    }

    private void doRollback(OrderCancelEventDTO event) {
        int rows = inventoryMapper.unlockStock(event.getProductId(), event.getCount());
        if (rows == 0) {
            log.error("库存回滚失败! productId={}, count={}, 可能已回滚或数据异常",
                    event.getProductId(), event.getCount());
        } else {
            log.info("库存回滚成功, productId={}", event.getProductId());
        }
    }
}