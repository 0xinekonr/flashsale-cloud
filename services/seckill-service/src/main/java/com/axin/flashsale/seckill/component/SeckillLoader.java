package com.axin.flashsale.seckill.component;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.seckill.entity.SeckillActivity;
import com.axin.flashsale.seckill.enums.SeckillActivityStatus;
import com.axin.flashsale.seckill.mapper.SeckillActivityMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 秒杀库存预热组件
 *
 * 负责将 DB 中的活动库存同步到 Redis，供 Lua 脚本原子扣减使用。
 *
 * 两种加载模式：
 * 1. 启动加载（@PostConstruct）：使用 setIfAbsent，不覆盖 Redis 中已有的数据。
 *    防止重启时重置已经被 Lua 脚本部分扣减的库存。
 * 2. 定时刷新（@Scheduled）：每 60 秒检查一次，仅当 Redis key 不存在时才从 DB 重新加载。
 *    主要用于应对 Redis 重启、key 意过期等场景，保证库存数据最终恢复。
 */
@Slf4j
@Component
public class SeckillLoader {

    @Autowired
    private SeckillActivityMapper mapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 应用启动时加载所有进行中活动的库存到 Redis
     *
     * 使用 setIfAbsent（不覆盖已有数据），防止重启时重置
     * 已经被 Lua 脚本部分扣减的库存。
     */
    @PostConstruct
    public void loadStock() {
        log.info("开始启动库存预热...");
        List<SeckillActivity> activities = mapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, SeckillActivityStatus.ONGOING.getCode()));

        if (activities.isEmpty()) {
            log.info("没有进行中的活动，跳过库存预热");
            return;
        }

        for (SeckillActivity activity : activities) {
            String stockKey = GlobalConstants.RedisKey.SECKILL_STOCK_PREFIX + activity.getId();
            Boolean set = redisTemplate.opsForValue()
                    .setIfAbsent(stockKey, String.valueOf(activity.getAvailableStock()));
            if (Boolean.TRUE.equals(set)) {
                log.info("库存预热完成: activityId={}, stock={}", activity.getId(), activity.getAvailableStock());
            } else {
                log.info("库存预热跳过（Redis 已有数据）: activityId={}", activity.getId());
            }
        }
        log.info("启动库存预热完成，共检查 {} 个活动", activities.size());
    }

    /**
     * 定时刷新：每 60 秒检查一次
     *
     * 仅当 Redis key 不存在时从 DB 重新加载。如果 key 存在，
     * 说明 Lua 脚本正在管理该库存，绝对不能覆盖（否则会重置已扣减的数量）。
     */
    @Scheduled(fixedRate = 60000)
    public void refreshStock() {
        List<SeckillActivity> activities = mapper.selectList(
                new LambdaQueryWrapper<SeckillActivity>()
                        .eq(SeckillActivity::getStatus, SeckillActivityStatus.ONGOING.getCode()));

        for (SeckillActivity activity : activities) {
            String key = GlobalConstants.RedisKey.SECKILL_STOCK_PREFIX + activity.getId();
            Boolean exists = redisTemplate.hasKey(key);

            if (Boolean.FALSE.equals(exists)) {
                // Redis key 丢失（可能 Redis 重启或 key 过期），从 DB 恢复
                redisTemplate.opsForValue().set(key, String.valueOf(activity.getAvailableStock()));
                log.warn("Redis 库存 key 丢失，已从 DB 恢复: activityId={}, stock={}",
                        activity.getId(), activity.getAvailableStock());
            }
            // key 存在则跳过，Lua 脚本是库存的唯一管理者
        }
    }

    /**
     * 预热单个活动的库存到 Redis
     *
     * 供活动发布时调用（SeckillActivityService.publish）。
     * 使用 set（覆盖模式），因为发布时 Redis 中一定没有该活动的 key。
     *
     * @param activityId 活动 ID
     */
    public void preloadActivity(Long activityId) {
        SeckillActivity activity = mapper.selectById(activityId);
        if (activity == null) {
            log.warn("预热失败: 活动不存在, activityId={}", activityId);
            return;
        }
        if (activity.getStatus() != SeckillActivityStatus.ONGOING.getCode()) {
            log.warn("预热跳过: 活动状态非进行中, activityId={}, status={}",
                    activityId, activity.getStatus());
            return;
        }

        String stockKey = GlobalConstants.RedisKey.SECKILL_STOCK_PREFIX + activityId;
        String userKey = GlobalConstants.RedisKey.SECKILL_USER_SET_PREFIX + activityId;

        redisTemplate.opsForValue().set(stockKey, String.valueOf(activity.getAvailableStock()));
        // 初始化用户去重 Set（清空可能残留的旧数据）
        redisTemplate.delete(userKey);

        log.info("库存预热完成: activityId={}, stock={}", activityId, activity.getAvailableStock());
    }
}
