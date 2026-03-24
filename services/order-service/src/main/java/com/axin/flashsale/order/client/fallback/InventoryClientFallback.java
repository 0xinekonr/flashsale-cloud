package com.axin.flashsale.order.client.fallback;

import com.axin.flashsale.order.client.InventoryClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public Boolean lockStock(Long productId, Integer count) {
        log.warn("触发熔断降级! Inventory Service 不可用, productId={}, count={}", productId, count);
        return false;
    }
}
