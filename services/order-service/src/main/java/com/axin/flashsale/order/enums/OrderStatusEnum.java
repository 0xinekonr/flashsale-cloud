package com.axin.flashsale.order.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum OrderStatusEnum {
    NEW(0, "待支付"),
    PAID(1, "已支付"),
    FULFILLED(2, "已发货"),
    CANCELLED(3, "已取消(超时未支付/主动取消)");

    private final Integer code;
    private final String desc;
}