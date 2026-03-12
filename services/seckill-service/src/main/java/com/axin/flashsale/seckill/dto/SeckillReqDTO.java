package com.axin.flashsale.seckill.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SeckillReqDTO {

    @NotNull(message = "用户ID不能为空")
    @Min(value = 1, message = "非法用户ID")
    private Long userId;

    @NotNull(message = "活动ID不能为空")
    @Min(value = 1, message = "非法活动ID")
    private Long activityId;
}
