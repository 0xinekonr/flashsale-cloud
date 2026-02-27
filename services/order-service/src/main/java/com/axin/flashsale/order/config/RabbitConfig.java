package com.axin.flashsale.order.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // 定义队列名常量
    public static final String ORDER_PAY_QUEUE = "order.pay.queue";

    public static final String SECKILL_ORDER_QUEUE = "seckill.order.queue";

    // 声明队列：持久化(true)
    @Bean
    public Queue orderPayQueue() {
        return new Queue(ORDER_PAY_QUEUE, true);
    }

    @Bean
    public Queue seckillOrderQueue() {
        return new Queue(SECKILL_ORDER_QUEUE, true);
    }
}
