package com.axin.flashsale.payment.config;

import com.axin.flashsale.common.mq.entity.OutboxMessage;
import com.axin.flashsale.common.mq.poller.OutboxPoller;
import com.axin.flashsale.common.mq.publisher.OutboxPublisher;
import com.axin.flashsale.payment.outbox.OutboxMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.List;

@Configuration
@EnableScheduling
public class OutboxConfig {

    @Bean
    public OutboxPublisher outboxPublisher(OutboxMapper outboxMapper, ObjectMapper objectMapper) {
        return new OutboxPublisher(
                message -> outboxMapper.insert(message),
                objectMapper
        );
    }

    @Bean
    public OutboxPoller outboxPoller(OutboxMapper outboxMapper, RabbitTemplate rabbitTemplate) {
        return new OutboxPoller(
                new OutboxPoller.OutboxRepository() {
                    @Override
                    public List<OutboxMessage> findPending(int limit) {
                        return outboxMapper.findPending(limit);
                    }

                    @Override
                    public void update(OutboxMessage message) {
                        outboxMapper.updateById(message);
                    }
                },
                rabbitTemplate,
                5,    // maxRetryCount
                100   // batchSize
        );
    }
}