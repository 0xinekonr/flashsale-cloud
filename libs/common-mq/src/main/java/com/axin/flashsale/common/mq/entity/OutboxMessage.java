package com.axin.flashsale.common.mq.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * Outbox 模式消息实体
 * 用于实现可靠的消息投递
 */
@Data
@TableName("outbox_message")
public class OutboxMessage {

    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 聚合类型（如 ORDER, PAYMENT）
     */
    private String aggregateType;

    /**
     * 聚合ID（如订单ID）
     */
    private Long aggregateId;

    /**
     * 事件类型（如 ORDER_CREATED, ORDER_CANCELLED）
     */
    private String eventType;

    /**
     * 消息载荷（JSON格式）
     */
    private String payload;

    /**
     * 状态：PENDING, SENT, FAILED
     */
    private String status;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 发送时间
     */
    private LocalDateTime sentTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    public static final String STATUS_PENDING = "PENDING";
    public static final String STATUS_SENT = "SENT";
    public static final String STATUS_FAILED = "FAILED";

    public static OutboxMessage create(String aggregateType, Long aggregateId, String eventType, String payload) {
        OutboxMessage msg = new OutboxMessage();
        msg.setAggregateType(aggregateType);
        msg.setAggregateId(aggregateId);
        msg.setEventType(eventType);
        msg.setPayload(payload);
        msg.setStatus(STATUS_PENDING);
        msg.setRetryCount(0);
        msg.setCreateTime(LocalDateTime.now());
        return msg;
    }
}