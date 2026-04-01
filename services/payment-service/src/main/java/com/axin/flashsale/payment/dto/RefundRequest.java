package com.axin.flashsale.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * 退款请求
 */
@Data
public class RefundRequest {
    /**
     * 退款金额，为空则全额退款
     */
    private BigDecimal amount;
}
