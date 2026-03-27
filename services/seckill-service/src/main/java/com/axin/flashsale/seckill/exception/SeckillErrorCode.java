package com.axin.flashsale.seckill.exception;

import com.axin.flashsale.common.exception.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SeckillErrorCode implements IResultCode {
    ACTIVITY_NOT_START(10001, "秒杀活动尚未开始"),
    STOCK_EMPTY(10002, "很遗憾，商品已被抢光"),
    REPEAT_ORDER(10003, "您已参与过该活动，请勿重复抢购"),
    ACTIVITY_NOT_FOUND(10004, "秒杀活动不存在"),
    ACTIVITY_ENDED(10005, "秒杀活动已结束"),
    ACTIVITY_DRAFT(10006, "秒杀活动尚未发布");

    private final Integer code;
    private final String message;
}
