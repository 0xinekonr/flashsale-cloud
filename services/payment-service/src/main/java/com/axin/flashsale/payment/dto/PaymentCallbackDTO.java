package com.axin.flashsale.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 支付回调请求（模拟第三方支付平台回调格式）
 */
@Data
public class PaymentCallbackDTO {
    /**
     * 支付流水ID
     */
    private Long paymentId;

    /**
     * 第三方交易流水号
     */
    private String transactionId;

    /**
     * 支付金额
     */
    private BigDecimal amount;

    /**
     * 支付状态 (SUCCESS/FAIL)
     */
    private String status;

    /**
     * 时间戳
     */
    private Long timestamp;

    /**
     * 签名类型 (MD5/RSA)
     */
    private String signType;

    /**
     * 签名
     */
    private String sign;
}