package com.axin.flashsale.order.config;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.redis.lock.DistributedLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedisConfig {

    @Bean
    public DistributedLock distributedLock(RedissonClient redissonClient) {
        return new DistributedLock(redissonClient, GlobalConstants.RedisKey.ORDER_LOCK_PREFIX);
    }
}