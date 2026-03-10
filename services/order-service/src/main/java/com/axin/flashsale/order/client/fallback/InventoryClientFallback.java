package com.axin.flashsale.order.client.fallback;

import com.axin.flashsale.order.client.InventoryClient;
import org.springframework.stereotype.Component;

@Component
public class InventoryClientFallback implements InventoryClient {

    @Override
    public Boolean lockStock(Long productId, Integer count) {
        System.err.println(" 触发熔断降级！Inventory Service 挂了，执行兜底逻辑。");
        // 这里可以返回 false，或者抛出特定异常让 Seata 知道要回滚
        return false;
    }
}
