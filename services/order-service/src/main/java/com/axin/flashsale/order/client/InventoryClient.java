package com.axin.flashsale.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

// name = "inventory-service" 必须与 Nacos 里的服务名一致
@FeignClient(name = "inventory-service")
public interface InventoryClient {

    /**
     * 对应 inventory-service 的 /inventory/{productId}/lock 接口
     * 注意：这里的 @RequestParam 必须写具体参数名
     */
    @PostMapping("/inventory/{productId}/lock")
    Boolean lockStock(@PathVariable Long productId, @RequestParam("count") Integer count);
}
