package com.axin.flashsale.seckill.component;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.seckill.entity.SeckillActivity;
import com.axin.flashsale.seckill.mapper.SeckillActivityMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class SeckillLoader {

    @Autowired
    private SeckillActivityMapper mapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PostConstruct
    public void loadStock() {
        List<SeckillActivity> activities = mapper.selectList(null);
        if (activities.isEmpty()) {
            return;
        }

        activities.forEach(activity -> {
            String key = GlobalConstants.RedisKey.SECKILL_STOCK_PREFIX + activity.getId();
            redisTemplate.opsForValue().setIfAbsent(key, String.valueOf(activity.getAvailableStock()));
            log.info("库存预热完成: {} -> {}", key, activity.getAvailableStock());
        });
    }
}
