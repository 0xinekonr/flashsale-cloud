package com.axin.flashsale.common.dto;

import lombok.Builder;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 订单取消事件广播 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCancelEventDTO implements Serializable {
    private Long orderId;
    private Long userId;
    private Long productId;
    private Integer count;
}