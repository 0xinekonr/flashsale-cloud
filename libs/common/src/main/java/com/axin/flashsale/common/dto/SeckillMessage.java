package com.axin.flashsale.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeckillMessage {
    private Long userId;
    private Long activityId;
    private Long productId;
    private BigDecimal seckillPrice;
}
