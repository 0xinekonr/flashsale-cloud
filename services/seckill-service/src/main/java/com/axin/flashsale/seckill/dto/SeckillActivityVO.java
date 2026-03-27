package com.axin.flashsale.seckill.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 秒杀活动响应 VO
 */
@Data
public class SeckillActivityVO {
    private Long id;
    private String activityName;
    private Long productId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalStock;
    private Integer availableStock;
    private BigDecimal seckillPrice;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
