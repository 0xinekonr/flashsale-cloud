package com.axin.flashsale.payment.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderDTO {
    private Long id;
    private Long userId;
    private Integer status;
    private BigDecimal totalAmount;
}
