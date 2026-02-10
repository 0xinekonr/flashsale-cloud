package com.axin.flashsale.payment.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@FeignClient(name = "order-service")
public interface OrderClient {

    // 调用订单服务更新状态
    @PostMapping("/orders/{id}/finish")
    Boolean finishOrder(@PathVariable Long id);
}
