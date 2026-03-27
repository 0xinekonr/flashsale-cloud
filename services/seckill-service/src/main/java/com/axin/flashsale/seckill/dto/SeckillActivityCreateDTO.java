package com.axin.flashsale.seckill.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 创建秒杀活动请求 DTO
 */
@Data
public class SeckillActivityCreateDTO {

    @NotBlank(message = "活动名称不能为空")
    private String activityName;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    @NotNull(message = "总库存不能为空")
    @Min(value = 1, message = "总库存至少为 1")
    private Integer totalStock;

    @NotNull(message = "秒杀价格不能为空")
    private BigDecimal seckillPrice;
}
