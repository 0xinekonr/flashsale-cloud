package com.axin.flashsale.common.mq.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 幂等消费者包装器
 * 基于 Redis 实现消息幂等消费
 */
@Slf4j
public class IdempotentConsumer {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;
    private final long ttlHours;

    public IdempotentConsumer(StringRedisTemplate redisTemplate, String keyPrefix, long ttlHours) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.ttlHours = ttlHours;
    }

    /**
     * 幂等消费消息
     * @param messageId 消息唯一ID
     * @param consumer 实际消费逻辑
     * @param <T> 消息类型
     * @return true 表示消费成功（首次或重复但成功），false 表示消费失败
     */
    public <T> boolean consume(String messageId, T message, Consumer<T> consumer) {
        String key = keyPrefix + messageId;

        // 使用 SETNX 实现幂等检查
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(key, "1", ttlHours, TimeUnit.HOURS);

        if (Boolean.FALSE.equals(isNew)) {
            log.warn("消息已消费过，跳过: messageId={}", messageId);
            return true; // 已消费过，视为成功（幂等）
        }

        try {
            consumer.accept(message);
            log.debug("消息消费成功: messageId={}", messageId);
            return true;
        } catch (Exception e) {
            log.error("消息消费失败，删除幂等标记: messageId={}", messageId, e);
            // 消费失败，删除标记以便重试
            redisTemplate.delete(key);
            throw e;
        }
    }

    /**
     * 检查消息是否已消费
     */
    public boolean isConsumed(String messageId) {
        String key = keyPrefix + messageId;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * 标记消息为已消费（手动确认场景）
     */
    public void markConsumed(String messageId) {
        String key = keyPrefix + messageId;
        redisTemplate.opsForValue().set(key, "1", ttlHours, TimeUnit.HOURS);
    }
}