package com.axin.flashsale.seckill.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 秒杀结果响应 VO
 */
@Data
@Builder
public class SeckillResultVO {
    /** 是否已参与秒杀（Redis Set 中存在该用户） */
    private boolean participated;
    /** 活动 ID */
    private Long activityId;
    /** 结果描述 */
    private String message;
}
