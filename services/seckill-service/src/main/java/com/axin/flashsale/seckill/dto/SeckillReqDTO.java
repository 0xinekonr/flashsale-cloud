package com.axin.flashsale.seckill.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 秒杀请求 DTO
 *
 * userId 不再由客户端传入，改为从 JWT token 中提取（见 UserContext），
 * 防止用户伪造身份冒充他人参与秒杀。
 */
@Data
public class SeckillReqDTO {

    @NotNull(message = "活动ID不能为空")
    @Min(value = 1, message = "非法活动ID")
    private Long activityId;
}
