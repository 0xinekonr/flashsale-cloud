package com.axin.flashsale.seckill.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 秒杀活动状态枚举
 *
 * 状态机流转: DRAFT → ONGOING → ENDED
 * - DRAFT(0): 草稿，管理员编辑中，不可参与秒杀
 * - ONGOING(1): 进行中，库存已预热到 Redis，允许秒杀
 * - ENDED(2): 已结束，不再接受秒杀请求
 */
@Getter
@AllArgsConstructor
public enum SeckillActivityStatus {

    DRAFT(0, "草稿"),
    ONGOING(1, "进行中"),
    ENDED(2, "已结束");

    private final int code;
    private final String description;
}
