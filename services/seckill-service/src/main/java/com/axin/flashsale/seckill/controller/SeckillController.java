package com.axin.flashsale.seckill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.axin.flashsale.common.exception.SystemCode;
import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.seckill.dto.SeckillReqDTO;
import com.axin.flashsale.seckill.service.SeckillService;
import com.axin.flashsale.seckill.util.UserContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 秒杀控制器
 *
 * @SentinelResource 定义受保护的资源，blockHandler 处理被限流/降级的请求。
 * userId 从 JWT token 中提取（UserContext），不再依赖客户端传入。
 */
@Slf4j
@RestController
@RequestMapping("/seckill")
public class SeckillController {

    @Autowired
    private SeckillService seckillService;

    @PostMapping("/do")
    @SentinelResource(value = "doSeckillResource", blockHandler = "seckillBlockHandler")
    public Result<String> doSeckill(@Validated @RequestBody SeckillReqDTO reqDTO) {
        // 从 JWT token 中提取用户 ID，而非请求体（防止身份伪造）
        Long userId = UserContext.getUserId();
        log.info("接收到秒杀请求, userId={}, activityId={}", userId, reqDTO.getActivityId());
        seckillService.seckill(reqDTO.getActivityId(), userId);
        return Result.success("恭喜！抢购成功");
    }

    /**
     * Sentinel 限流/降级处理方法
     * 方法签名必须与原方法一致，额外追加 BlockException 参数
     */
    public Result<String> seckillBlockHandler(SeckillReqDTO reqDTO, BlockException ex) {
        log.warn("触发 Sentinel 限流, activityId={}", reqDTO.getActivityId());
        return Result.fail(SystemCode.FLOW_LIMITED);
    }
}
