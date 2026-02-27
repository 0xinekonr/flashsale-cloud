package com.axin.flashsale.seckill.service;

import com.axin.flashsale.common.dto.SeckillMessage;
import com.axin.flashsale.seckill.entity.SeckillActivity;
import com.axin.flashsale.seckill.mapper.SeckillActivityMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SeckillService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private SeckillActivityMapper activityMapper;   // 用于获取商品ID和价格

    // 初始化 Lua 脚本对象
    private DefaultRedisScript<Long> stockScript;

    public SeckillService() {
        stockScript = new DefaultRedisScript<>();
        stockScript.setLocation(new ClassPathResource("/scripts/stock.lua"));
        stockScript.setResultType(Long.class);
    }

    /**
     * 核心秒杀逻辑 (Level 1: Redis 预扣)
     * @param activityId 活动ID/商品ID
     * @param userId 用户ID
     * @return true=抢到了（预扣成功）， false=没抢到
     */
    public boolean seckill(Long activityId, Long userId) {
        // 1. 【防重】利用 Redis Set 检查用户是否已经抢购
        // Key: seckill:users:{activityId}
        String userKey = "seckill:users:" + activityId;
        Boolean isMember = redisTemplate.opsForSet().isMember(userKey, userId.toString());
        if (Boolean.TRUE.equals(isMember)) {
//            System.out.println("用户 " + userId + "重复抢购");
            return false;
        }

        // 2. 【预扣】执行 Lua 脚本扣库存
        String key = "seckill:stock:" + activityId;
        Long result = redisTemplate.execute(stockScript, Collections.singletonList(key), "1");

        if (result.equals(0L)) {
            return false;   //  库存不足
        }

        // 3. 【占位】将用户加入已抢购集合（防止重复）
        redisTemplate.opsForSet().add(userKey, userId.toString());

        // 4. 【削峰】发送消息到 MQ，异步创建订单
        // 为了演示方便，这里现查一下数据库获取商品信息（生产环境应走缓存）
        SeckillActivity activity = activityMapper.selectById(activityId);

        SeckillMessage message = new SeckillMessage(userId, activityId, activity.getProductId(), activity.getSeckillPrice());

        // 发送到 "seckill.order.queue"
        rabbitTemplate.convertAndSend("seckill.order.queue", message);
//        System.out.println("秒杀成功，消息已发送" + userId);

        return true;
    }
}
