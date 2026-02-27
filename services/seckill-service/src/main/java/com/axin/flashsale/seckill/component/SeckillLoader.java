package com.axin.flashsale.seckill.component;

import com.axin.flashsale.seckill.entity.SeckillActivity;
import com.axin.flashsale.seckill.mapper.SeckillActivityMapper;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SeckillLoader {

    @Autowired
    private SeckillActivityMapper mapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 服务启动后执行：将 MySQL 中的库存同步到 Redis
     * Redis Key 格式: seckill:stock:{activityId}
     */
    @PostConstruct
    public void loadStock() {
        List<SeckillActivity> activities = mapper.selectList(null);
        if (activities.isEmpty()) {return;}

        activities.forEach(activity -> {
            String key = "seckill:stock:" + activity.getId();
            // 只有当 Redis 里没有这个 Key 时才设置，防止覆盖
            redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(activity.getAvailableStock()));
            System.out.println("库存预热完成：" + key + " -> " + activity.getAvailableStock());
        });
    }
}
