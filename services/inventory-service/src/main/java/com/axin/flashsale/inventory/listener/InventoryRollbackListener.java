package com.axin.flashsale.inventory.listener;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.dto.OrderCancelEventDTO;
import com.axin.flashsale.inventory.mapper.InventoryMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryRollbackListener {

    @Autowired
    private InventoryMapper inventoryMapper;

    @RabbitListener(queues = GlobalConstants.MQ.INVENTORY_ROLLBACK_QUEUE)
    public void handleStockRollback(OrderCancelEventDTO event) {
        log.info("收到库存回滚事件, orderId={}, productId={}, count={}",
                event.getOrderId(), event.getProductId(), event.getCount());
        int rows = inventoryMapper.unlockStock(event.getProductId(), event.getCount());
        if (rows == 0) {
            log.error("库存回滚失败! productId={}, count={}, 可能已回滚或数据异常",
                    event.getProductId(), event.getCount());
        } else {
            log.info("库存回滚成功, productId={}", event.getProductId());
        }
    }
}