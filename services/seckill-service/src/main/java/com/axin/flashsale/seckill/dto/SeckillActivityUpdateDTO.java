package com.axin.flashsale.seckill.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 更新秒杀活动请求 DTO（所有字段可选，部分更新）
 */
@Data
public class SeckillActivityUpdateDTO {

    private String activityName;
    private Long productId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalStock;
    private BigDecimal seckillPrice;
}
