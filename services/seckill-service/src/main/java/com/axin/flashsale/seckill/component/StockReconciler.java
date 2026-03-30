package com.axin.flashsale.seckill.component;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.redis.lock.DistributedLock;
import com.axin.flashsale.seckill.entity.SeckillActivity;
import com.axin.flashsale.seckill.enums.SeckillActivityStatus;
import com.axin.flashsale.seckill.mapper.SeckillActivityMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 库存对账组件
 *
 * 定期对比 Redis 和 DB 的库存数据，修复漂移。
 *
 * 为什么需要对账：
 * - Redis Lua 脚本扣减库存后，DB 的 deductStock() 可能偶发失败（网络抖动等）
 * - 需要确保 Redis 和 DB 最终一致
 *
 * 使用 DistributedLock 保证多实例部署时只有一个实例执行对账，
 * 避免并发对账导致的数据混乱。
 */
@Slf4j
@Component
public class StockReconciler {

    @Autowired
    private SeckillActivityMapper activityMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DistributedLock distributedLock;

    /**
     * 每 5 分钟执行一次库存对账
     *
     * 使用 tryLock with waitTime=0（不等待获取锁），如果其他实例已在执行则跳过。
     * 锁持有时间 30 秒，足够完成对账操作。
     */
    @Scheduled(fixedRate = 300000)
    public void reconcile() {
        boolean executed = distributedLock.tryLock("stock:reconcile", 0, 30, TimeUnit.SECONDS, () -> {
            doReconcile();
        });
        if (!executed) {
            log.debug("未获取到对账锁，跳过本次执行（其他实例正在对账）");
        }
    }

    /**
     * 执行实际的对账逻辑
     */
    private void doReconcile() {
        log.info("开始库存对账...");
        int fixedCount = 0;

        List<SeckillActivity> activities = activityMapper.selectList(null);
        if (activities.isEmpty()) {
            log.info("没有活动，跳过对账");
            return;
        }

        for (SeckillActivity activity : activities) {
            int fixed = reconcileActivity(activity);
            fixedCount += fixed;
        }

        log.info("库存对账完成, 共检查 {} 个活动, 修复 {} 处漂移", activities.size(), fixedCount);
    }

    /**
     * 对账单个活动的库存
     *
     * 以 Redis 为权威数据源（Lua 脚本是库存的唯一管理者），
     * 发现漂移时将 DB 修正为 Redis 的值。
     *
     * @return 修复的漂移数量（0 或 1）
     */
    private int reconcileActivity(SeckillActivity activity) {
        // 只对进行中的活动做对账
        if (activity.getStatus() != SeckillActivityStatus.ONGOING.getCode()) {
            return 0;
        }

        String stockKey = GlobalConstants.RedisKey.SECKILL_STOCK_PREFIX + activity.getId();
        String redisValue = redisTemplate.opsForValue().get(stockKey);
        int dbStock = activity.getAvailableStock();

        // Redis key 不存在：跳过（由 SeckillLoader.refreshStock 处理）
        if (redisValue == null) {
            log.warn("对账发现 Redis key 丢失, 等待 SeckillLoader 恢复. activityId={}", activity.getId());
            return 0;
        }

        int redisStock = Integer.parseInt(redisValue);

        if (redisStock != dbStock) {
            // DB 扣减失败导致漂移：以 Redis 为准修正 DB
            activityMapper.setStock(activity.getId(), redisStock);
            log.warn("对账修复: 库存漂移已修正. activityId={}, redis={}, db={}, corrected={}",
                    activity.getId(), redisStock, dbStock, redisStock);
            return 1;
        }

        return 0;
    }
}
