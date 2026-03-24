package com.axin.flashsale.common.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 商品 DTO（用于Feign调用）
 */
@Data
public class ProductDTO implements Serializable {
    private Long id;
    private String name;
    private String description;
    private Long categoryId;
    private Integer status;
    private List<ProductSkuDTO> skus;
}
