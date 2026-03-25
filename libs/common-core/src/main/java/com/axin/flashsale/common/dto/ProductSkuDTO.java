package com.axin.flashsale.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 商品SKU DTO（用于Feign调用）
 */
@Data
public class ProductSkuDTO implements Serializable {
    private Long id;
    private Long productId;
    private String skuName;
    private BigDecimal price;
    private Integer stock;
    private String attributes;
    private Integer status;
}
