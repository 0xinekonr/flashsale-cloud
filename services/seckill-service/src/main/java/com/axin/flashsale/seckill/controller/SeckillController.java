package com.axin.flashsale.seckill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.axin.flashsale.common.exception.SystemCode;
import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.seckill.dto.SeckillReqDTO;
import com.axin.flashsale.seckill.service.SeckillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
    @PostMapping("/do")
    @SentinelResource(value = "doSeckillResource", blockHandler = "seckillBlockHandler")
    public Result<String> doSeckill(@Validated @RequestBody SeckillReqDTO reqDTO) {
        log.info("接收到秒杀请求, userId={}", reqDTO.getUserId());
        seckillService.seckill(reqDTO.getActivityId(), reqDTO.getUserId());
        return Result.success("恭喜！抢购成功");
    }

    public Result<String> seckillBlockHandler(SeckillReqDTO reqDTO, BlockException ex) {
        log.warn("触发 Sentinel 限流, userId={}", reqDTO.getUserId());
        return Result.fail(SystemCode.FLOW_LIMITED);
    }
}

