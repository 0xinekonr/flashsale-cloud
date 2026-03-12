package com.axin.flashsale.seckill.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.axin.flashsale.common.exception.BizException;
import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.seckill.dto.SeckillReqDTO;
import com.axin.flashsale.seckill.exception.SeckillErrorCode;
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
        log.info("接收到秒杀请求，userId: {}", reqDTO.getUserId());
        boolean success = seckillService.seckill(reqDTO.getActivityId(), reqDTO.getUserId());
        if (success) {
            return Result.success("恭喜！抢购成功（Redis预扣）");
        } else {
            // 失败时，直接抛出业务异常。GlobalExceptionHandler 会自动将其转换为 Result.fail(...)
            throw new BizException(SeckillErrorCode.STOCK_EMPTY);
        }

    }

    /**
     * 限流/熔断时的兜底处理方法 (Fallback / BlockHandler)
     * 注意：
     * 1. 必须是 public
     * 2. 返回值类型、参数列表必须和原方法完全一致！
     * 3. 必须在参数列表最后多加一个 BlockException 参数！
     */
    public String seckillBlockHandler(SeckillReqDTO reqDTO, BlockException ex) {
        System.out.println("触发 Sentinel 限流！拦截了 userId: " + reqDTO.getUserId());
        return "哎呀，活动太火爆啦，请稍后再试！（被 Sentinel 保护）";
    }
}

