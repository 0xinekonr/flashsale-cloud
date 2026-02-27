package com.axin.flashsale.seckill.controller;

import com.axin.flashsale.seckill.service.SeckillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    /**
     * 秒杀接口
     */
    @PostMapping("/{activityId}")
    public String doSeckill(@PathVariable Long activityId, @RequestParam Long userId) {
        boolean success = seckillService.seckill(activityId, userId);
        return success ? "恭喜！抢购成功（Redis预扣）" : "很遗憾，没抢到";
    }
}

