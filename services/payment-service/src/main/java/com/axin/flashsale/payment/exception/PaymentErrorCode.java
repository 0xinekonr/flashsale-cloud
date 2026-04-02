package com.axin.flashsale.payment.exception;

import com.axin.flashsale.common.exception.IResultCode;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 支付服务错误码
 */
@Getter
@AllArgsConstructor
public enum PaymentErrorCode implements IResultCode {
    PAYMENT_NOT_FOUND(20001, "支付记录不存在"),
    PAYMENT_ALREADY_PROCESSED(20002, "支付已处理"),
    SIGNATURE_INVALID(20003, "支付签名无效"),
    ORDER_ALREADY_PAID(20004, "订单已支付"),
    ORDER_NOT_FOUND(20005, "订单不存在"),
    ORDER_STATUS_INVALID(20006, "订单状态不允许支付"),
    REFUND_NOT_ALLOWED(20007, "该支付不允许退款"),
    ORDER_SERVICE_UNAVAILABLE(20008, "订单服务暂时不可用");

    private final Integer code;
    private final String message;
}
