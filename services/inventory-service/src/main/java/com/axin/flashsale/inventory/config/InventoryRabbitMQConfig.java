package com.axin.flashsale.inventory.config;

import com.axin.flashsale.common.constant.GlobalConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class InventoryRabbitMQConfig {

    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange(GlobalConstants.MQ.ORDER_EVENT_EXCHANGE, true, false);
    }

    @Bean
    public Queue inventoryRollbackQueue() {
        return new Queue(GlobalConstants.MQ.INVENTORY_ROLLBACK_QUEUE, true);
    }

    @Bean
    public Binding inventoryRollbackBinding() {
        return BindingBuilder.bind(inventoryRollbackQueue()).to(orderEventExchange()).with(GlobalConstants.MQ.ORDER_CANCEL_NOTIFY_ROUTING_KEY);

    }
}