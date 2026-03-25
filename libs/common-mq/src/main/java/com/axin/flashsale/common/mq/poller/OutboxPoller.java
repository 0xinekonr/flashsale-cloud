package com.axin.flashsale.common.mq.poller;

import com.axin.flashsale.common.mq.entity.OutboxMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

/**
 * Outbox 消息轮询器
 * 定时扫描 PENDING 状态的消息并投递到 MQ
 */
@Slf4j
public class OutboxPoller {

    private final OutboxRepository repository;
    private final RabbitTemplate rabbitTemplate;
    private final int maxRetryCount;
    private final int batchSize;

    public OutboxPoller(OutboxRepository repository, RabbitTemplate rabbitTemplate,
                        int maxRetryCount, int batchSize) {
        this.repository = repository;
        this.rabbitTemplate = rabbitTemplate;
        this.maxRetryCount = maxRetryCount;
        this.batchSize = batchSize;
    }

    /**
     * 定时扫描并投递消息（每3秒执行一次）
     */
    @Scheduled(fixedDelay = 3000)
    public void pollAndPublish() {
        List<OutboxMessage> messages = repository.findPending(batchSize);
        if (messages.isEmpty()) {
            return;
        }

        log.debug("扫描到 {} 条待发送的 Outbox 消息", messages.size());

        for (OutboxMessage message : messages) {
            try {
                publishMessage(message);
                message.setStatus(OutboxMessage.STATUS_SENT);
                message.setSentTime(LocalDateTime.now());
                repository.update(message);
                log.info("Outbox消息投递成功: id={}, eventType={}", message.getId(), message.getEventType());
            } catch (Exception e) {
                log.error("Outbox消息投递失败: id={}", message.getId(), e);
                handleFailure(message);
            }
        }
    }

    private void publishMessage(OutboxMessage message) {
        // 根据事件类型路由到不同的交换机
        String exchange = resolveExchange(message.getAggregateType(), message.getEventType());
        String routingKey = resolveRoutingKey(message.getEventType());

        rabbitTemplate.convertAndSend(exchange, routingKey, message.getPayload());
    }

    private void handleFailure(OutboxMessage message) {
        message.setRetryCount(message.getRetryCount() + 1);
        if (message.getRetryCount() >= maxRetryCount) {
            message.setStatus(OutboxMessage.STATUS_FAILED);
            log.error("Outbox消息超过最大重试次数，标记为失败: id={}, retryCount={}",
                    message.getId(), message.getRetryCount());
        }
        repository.update(message);
    }

    /**
     * 根据聚合类型和事件类型解析交换机名称
     * 子类可覆盖实现自定义路由逻辑
     */
    protected String resolveExchange(String aggregateType, String eventType) {
        return aggregateType.toLowerCase() + ".event.exchange";
    }

    /**
     * 根据事件类型解析路由键
     */
    protected String resolveRoutingKey(String eventType) {
        return eventType.toLowerCase().replace("_", ".");
    }

    /**
     * Outbox 存储接口（由使用方实现）
     */
    public interface OutboxRepository {
        List<OutboxMessage> findPending(int limit);
        void update(OutboxMessage message);
    }
}