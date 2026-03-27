package com.axin.flashsale.seckill.service;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.dto.SeckillMessage;
import com.axin.flashsale.common.exception.BizException;
import com.axin.flashsale.common.exception.SystemCode;
import com.axin.flashsale.seckill.entity.SeckillActivity;
import com.axin.flashsale.seckill.enums.SeckillActivityStatus;
import com.axin.flashsale.seckill.exception.SeckillErrorCode;
import com.axin.flashsale.seckill.mapper.SeckillActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Slf4j
@Service
public class SeckillService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SeckillActivityMapper activityMapper;

    private final DefaultRedisScript<Long> stockScript;

    public SeckillService() {
        stockScript = new DefaultRedisScript<>();
        stockScript.setLocation(new ClassPathResource("/scripts/stock.lua"));
        stockScript.setResultType(Long.class);
    }

    public boolean seckill(Long activityId, Long userId) {
        // 1. 校验活动是否存在
        SeckillActivity activity = activityMapper.selectById(activityId);
        if (activity == null) {
            throw new BizException(SeckillErrorCode.ACTIVITY_NOT_FOUND);
        }

        // 2. 校验活动状态（状态机: DRAFT → ONGOING → ENDED）
        if (activity.getStatus() == SeckillActivityStatus.ENDED.getCode()) {
            throw new BizException(SeckillErrorCode.ACTIVITY_ENDED);
        }
        if (activity.getStatus() == SeckillActivityStatus.DRAFT.getCode()) {
            throw new BizException(SeckillErrorCode.ACTIVITY_DRAFT);
        }

        // 3. 校验活动时间窗口（即使状态为 ONGOING，也做时间兜底校验）
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(activity.getStartTime()) || now.isAfter(activity.getEndTime())) {
            throw new BizException(SeckillErrorCode.ACTIVITY_NOT_START);
        }

        // 4. 原子去重：SADD 返回 0 表示已存在
        String userKey = GlobalConstants.RedisKey.SECKILL_USER_SET_PREFIX + activityId;
        Long added = redisTemplate.opsForSet().add(userKey, userId.toString());
        if (added == null || added == 0) {
            throw new BizException(SeckillErrorCode.REPEAT_ORDER);
        }

        // 5. Lua 原子扣减
        String stockKey = GlobalConstants.RedisKey.SECKILL_STOCK_PREFIX + activityId;
        Long result = redisTemplate.execute(stockScript, Collections.singletonList(stockKey), "1");

        if (result == null || result == -1L) {
            // key 不存在，回滚用户标记
            redisTemplate.opsForSet().remove(userKey, userId.toString());
            throw new BizException(SeckillErrorCode.ACTIVITY_NOT_START);
        }
        if (result == 0L) {
            // 库存不足，回滚用户标记
            redisTemplate.opsForSet().remove(userKey, userId.toString());
            throw new BizException(SeckillErrorCode.STOCK_EMPTY);
        }

        // 6. 发送 MQ 消息
        SeckillMessage message = new SeckillMessage(
                userId, activityId, activity.getProductId(), activity.getSeckillPrice());
        try {
            rabbitTemplate.convertAndSend(
                    GlobalConstants.MQ.SECKILL_EXCHANGE,
                    GlobalConstants.MQ.SECKILL_ROUTING_KEY, message);
        } catch (Exception e) {
            // MQ 发送失败：回补 Redis 库存 + 移除用户标记
            log.error("秒杀消息发送失败, 回补库存. activityId={}, userId={}", activityId, userId, e);
            redisTemplate.opsForValue().increment(stockKey);
            redisTemplate.opsForSet().remove(userKey, userId.toString());
            throw new BizException(SystemCode.SYSTEM_ERROR);
        }
        return true;
    }
}
