package com.axin.flashsale.order.config;

import com.axin.flashsale.common.constant.GlobalConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class OrderRabbitMQConfig {

    // 声明队列：持久化(true)
    @Bean
    public Queue orderPayQueue() {
        return new Queue(GlobalConstants.MQ.ORDER_PAY_QUEUE, true);
    }

    @Bean
    public Queue seckillOrderQueue() {
        return new Queue(GlobalConstants.MQ.SECKILL_ORDER_QUEUE, true);
    }

    // ================= 新增：超时取消延迟队列架构 =================
    /**
     * 1. 声明订单事件统一交换机 (Topic)
     * 以后订单模块的所有消息（创建、支付、发货）都可以往这里发，通过 RoutingKey 分发
     */
    @Bean
    public TopicExchange orderEventExchange() {
        return new TopicExchange(GlobalConstants.MQ.ORDER_EVENT_EXCHANGE, true, false);
    }

    /**
     * 2. 声明延迟队列 (核心魔法：消息在这里等死)
     * 注意：这个队列绝对不能有任何 @RabbitListener 监听它！
     */
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        // 绑定死信交换机 (死亡后去哪)
        args.put("x-dead-letter-exchange", GlobalConstants.MQ.ORDER_EVENT_EXCHANGE);
        // 死信路由键 (带着什么Key去投胎)
        args.put("x-dead-letter-routing-key", GlobalConstants.MQ.ORDER_CANCEL_NOTIFY_ROUTING_KEY);
        // 消息存活时间 TTL (真实业务15分钟=900000，这里测试设为 30 秒 = 30000)
        args.put("x-message-ttl", 30000);

        return new Queue(GlobalConstants.MQ.ORDER_DELAY_QUEUE, true, false, false, args);
    }

    /**
     * 3. 声明死信队列 (超时被处决的消息会掉落到这里，供 Listener 消费)
     */
    @Bean
    public Queue orderDeadLetterQueue() {
        return new Queue(GlobalConstants.MQ.ORDER_DEAD_LETTER_QUEUE, true, false, false);
    }

    /**
     * 4. 绑定延迟队列到交换机 (路由键: order.create)
     */
    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue()).to(orderEventExchange()).with(GlobalConstants.MQ.ORDER_CREATE_ROUTING_KEY);
    }

    /**
     * 5. 绑定死信队列到交换机 (路由键: order.cancel)
     */
    @Bean
    public Binding orderDeadLetterBinding() {
        return BindingBuilder.bind(orderDeadLetterQueue()).to(orderEventExchange()).with(GlobalConstants.MQ.ORDER_CANCEL_NOTIFY_ROUTING_KEY);
    }
}