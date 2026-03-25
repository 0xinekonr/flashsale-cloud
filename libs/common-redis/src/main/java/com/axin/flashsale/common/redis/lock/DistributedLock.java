package com.axin.flashsale.common.redis.lock;

import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * 基于 Redisson 的分布式锁工具类
 */
@Slf4j
public class DistributedLock {

    private final RedissonClient redissonClient;
    private final String lockPrefix;

    public DistributedLock(RedissonClient redissonClient, String lockPrefix) {
        this.redissonClient = redissonClient;
        this.lockPrefix = lockPrefix;
    }

    /**
     * 尝试获取锁并执行任务
     * @param lockKey 锁的key
     * @param waitTime 等待时间
     * @param leaseTime 持有时间
     * @param unit 时间单位
     * @param supplier 要执行的任务
     * @return 任务执行结果，获取锁失败返回 null
     */
    public <T> T tryLock(String lockKey, long waitTime, long leaseTime,
                         TimeUnit unit, Supplier<T> supplier) {
        String fullKey = lockPrefix + lockKey;
        RLock lock = redissonClient.getLock(fullKey);

        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                log.warn("获取分布式锁失败: key={}", fullKey);
                return null;
            }
            log.debug("获取分布式锁成功: key={}", fullKey);
            return supplier.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: key={}", fullKey, e);
            return null;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁: key={}", fullKey);
            }
        }
    }

    /**
     * 尝试获取锁并执行无返回值任务
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime,
                           TimeUnit unit, Runnable runnable) {
        String fullKey = lockPrefix + lockKey;
        RLock lock = redissonClient.getLock(fullKey);

        try {
            boolean acquired = lock.tryLock(waitTime, leaseTime, unit);
            if (!acquired) {
                log.warn("获取分布式锁失败: key={}", fullKey);
                return false;
            }
            log.debug("获取分布式锁成功: key={}", fullKey);
            runnable.run();
            return true;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("获取分布式锁被中断: key={}", fullKey, e);
            return false;
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.debug("释放分布式锁: key={}", fullKey);
            }
        }
    }

    /**
     * 快速尝试获取锁（不等待）
     */
    public <T> T tryLockNoWait(String lockKey, long leaseTime,
                               TimeUnit unit, Supplier<T> supplier) {
        return tryLock(lockKey, 0, leaseTime, unit, supplier);
    }
}