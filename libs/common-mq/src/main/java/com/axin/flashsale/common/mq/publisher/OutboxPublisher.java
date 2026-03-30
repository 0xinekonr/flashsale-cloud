package com.axin.flashsale.common.mq.publisher;

import com.axin.flashsale.common.mq.entity.OutboxMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Outbox 消息发布器
 * 在业务事务中调用，将消息写入 outbox 表（与业务操作在同一个本地事务中）
 */
@Slf4j
public class OutboxPublisher {

    private final OutboxRepository repository;
    private final ObjectMapper objectMapper;

    public OutboxPublisher(OutboxRepository repository, ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    /**
     * 发布事件到 outbox
     * @param aggregateType 聚合类型（如 ORDER）
     * @param aggregateId 聚合ID
     * @param eventType 事件类型（如 ORDER_CREATED）
     * @param payload 事件载荷对象（会被序列化为JSON）
     */
    public void publish(String aggregateType, Long aggregateId, String eventType, Object payload) {
        try {
            String payloadJson = objectMapper.writeValueAsString(payload);
            OutboxMessage message = OutboxMessage.create(aggregateType, aggregateId, eventType, payloadJson);
            repository.save(message);
            log.debug("Outbox消息已保存: aggregateType={}, aggregateId={}, eventType={}",
                    aggregateType, aggregateId, eventType);
        } catch (JsonProcessingException e) {
            log.error("序列化消息载荷失败: {}", payload, e);
            throw new RuntimeException("序列化消息载荷失败", e);
        }
    }

    /**
     * Outbox 存储接口（由使用方实现）
     */
    public interface OutboxRepository {
        void save(OutboxMessage message);
    }
}