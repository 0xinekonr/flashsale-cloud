package com.axin.flashsale.seckill.config;

import com.axin.flashsale.common.redis.lock.DistributedLock;
import org.redisson.api.RedissonClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis 配置类
 *
 * 注册分布式锁 Bean，供 StockReconciler 等组件使用。
 * DistributedLock 基于 Redisson 的 RLock 实现，
 * 提供了带超时的 tryLock 语义，防止死锁。
 */
@Configuration
public class RedisConfig {

    @Bean
    public DistributedLock distributedLock(RedissonClient redissonClient) {
        return new DistributedLock(redissonClient, "lock:");
    }
}
