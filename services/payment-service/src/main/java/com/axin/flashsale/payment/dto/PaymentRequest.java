package com.axin.flashsale.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 创建支付请求
 */
@Data
public class PaymentRequest {
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
}
