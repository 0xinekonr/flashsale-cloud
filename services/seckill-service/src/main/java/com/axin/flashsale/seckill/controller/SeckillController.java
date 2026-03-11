package com.axin.flashsale.seckill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.axin.flashsale.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * SentinelResource 定义了一个受保护的资源
     * value：资源名称（在控制台里显示的名称）
     * blockHandler：被限流/降级时，去调用哪个方法处理
     * 秒杀接口
     */
    @PostMapping("/{activityId}")
    @SentinelResource(value = "doSeckillResource", blockHandler = "seckillBlockHandler")
    public String doSeckill(@PathVariable Long activityId, @RequestParam Long userId) {
        log.info("接收到秒杀请求，userId: {}", userId);
        boolean success = seckillService.seckill(activityId, userId);
        return success ? "恭喜！抢购成功（Redis预扣）" : "很遗憾，没抢到";
    }

    /**
     * 限流/熔断时的兜底处理方法 (Fallback / BlockHandler)
     * 注意：
     * 1. 必须是 public
     * 2. 返回值类型、参数列表必须和原方法完全一致！
     * 3. 必须在参数列表最后多加一个 BlockException 参数！
     */
    public String seckillBlockHandler(Long activityId, Long userId, BlockException ex) {
        System.out.println("触发 Sentinel 限流！拦截了 userId: " + userId);
        return "哎呀，活动太火爆啦，请稍后再试！（被 Sentinel 保护）";
    }
}

