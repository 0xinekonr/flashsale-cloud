package com.axin.flashsale.common.redis.interceptor;

import com.axin.flashsale.common.exception.BizException;
import com.axin.flashsale.common.exception.SystemCode;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的 HTTP 请求幂等拦截器
 * 通过 X-Idempotent-Key Header 实现请求幂等
 */
@Slf4j
public class IdempotentKeyInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;
    private final String keyPrefix;
    private final long ttlSeconds;

    public IdempotentKeyInterceptor(StringRedisTemplate redisTemplate,
                                     String keyPrefix, long ttlSeconds) {
        this.redisTemplate = redisTemplate;
        this.keyPrefix = keyPrefix;
        this.ttlSeconds = ttlSeconds;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String idempotentKey = request.getHeader("X-Idempotent-Key");
        if (idempotentKey == null || idempotentKey.isEmpty()) {
            // 没有幂等键，放行（不强制要求）
            return true;
        }

        String redisKey = keyPrefix + idempotentKey;

        // SETNX 实现幂等检查
        Boolean isNew = redisTemplate.opsForValue()
                .setIfAbsent(redisKey, "1", ttlSeconds, TimeUnit.SECONDS);

        if (Boolean.FALSE.equals(isNew)) {
            log.warn("重复请求被拦截, idempotentKey={}", idempotentKey);
            throw new BizException(SystemCode.IDEMPOTENT_REJECT);
        }

        log.debug("幂等检查通过, idempotentKey={}", idempotentKey);
        return true;
    }
}