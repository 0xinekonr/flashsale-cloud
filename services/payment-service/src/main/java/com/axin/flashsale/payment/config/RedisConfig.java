package com.axin.flashsale.payment.config;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.mq.consumer.IdempotentConsumer;
import com.axin.flashsale.common.redis.lock.DistributedLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;


@Configuration
public class RedisConfig {

    @Bean
    public DistributedLock distributedLock(RedissonClient redissonClient) {
        return new DistributedLock(redissonClient, GlobalConstants.RedisKey.PAYMENT_LOCK_PREFIX);
    }

    @Bean
    public IdempotentConsumer idempotentConsumer(StringRedisTemplate redisTemplate) {
        // 支付回调幂等，TTL 72 小时
        return new IdempotentConsumer(redisTemplate, GlobalConstants.RedisKey.PAYMENT_CALLBACK_PREFIX, 72);
    }
}
