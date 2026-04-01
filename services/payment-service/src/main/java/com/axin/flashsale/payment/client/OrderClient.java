package com.axin.flashsale.payment.client;

import com.axin.flashsale.payment.client.fallback.OrderClientFallback;
import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.payment.dto.OrderDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "order-service", fallback = OrderClientFallback.class)
public interface OrderClient {

    @GetMapping("/orders/{orderId}")
    Result<OrderDTO> getOrder(@PathVariable("orderId") Long orderId);
}
