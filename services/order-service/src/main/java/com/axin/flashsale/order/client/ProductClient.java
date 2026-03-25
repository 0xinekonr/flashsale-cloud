package com.axin.flashsale.order.client;

import com.axin.flashsale.common.dto.ProductSkuDTO;
import com.axin.flashsale.order.client.fallback.ProductClientFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "product-service", fallback = ProductClientFallback.class)
public interface ProductClient {

    @GetMapping("/products/{productId}/skus/{skuId}")
    ProductSkuDTO getSku(@PathVariable Long productId, @PathVariable Long skuId);

    @GetMapping("/products/{productId}/skus/{skuId}/price")
    java.math.BigDecimal getSkuPrice(@PathVariable Long productId, @PathVariable Long skuId);
}
