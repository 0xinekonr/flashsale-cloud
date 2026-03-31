package com.axin.flashsale.payment.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付状态枚举
 */
@Getter
@AllArgsConstructor
public enum PaymentStatusEnum {
    PENDING(0, "待支付"),
    SUCCESS(1, "支付成功"),
    FAILED(2, "支付失败"),
    REFUNDED(3, "已退款");

    private final Integer code;
    private final String desc;

    public static PaymentStatusEnum fromCode(Integer code) {
        for (PaymentStatusEnum status : values()) {
            if (status.getCode().equals(code)) {
                return status;
            }
        }
        return null;
    }
}
