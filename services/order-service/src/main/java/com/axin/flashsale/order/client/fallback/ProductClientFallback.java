package com.axin.flashsale.order.client.fallback;

import com.axin.flashsale.common.dto.ProductSkuDTO;
import com.axin.flashsale.order.client.ProductClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
public class ProductClientFallback implements ProductClient {

    @Override
    public ProductSkuDTO getSku(Long productId, Long skuId) {
        log.warn("ProductClient.getSku 降级, productId={}, skuId={}", productId, skuId);
        return null;
    }

    @Override
    public BigDecimal getSkuPrice(Long productId, Long skuId) {
        log.warn("ProductClient.getSkuPrice 降级, productId={}, skuId={}", productId, skuId);
        return null;
    }
}
