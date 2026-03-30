package com.axin.flashsale.seckill.controller;

import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.seckill.dto.SeckillActivityCreateDTO;
import com.axin.flashsale.seckill.dto.SeckillActivityUpdateDTO;
import com.axin.flashsale.seckill.dto.SeckillActivityVO;
import com.axin.flashsale.seckill.service.SeckillActivityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 秒杀活动管理控制器
 *
 * 提供活动的创建、编辑、发布、结束、查询等管理接口。
 * 当前所有接口开放访问（后续可加管理员鉴权中间件）。
 */
@Slf4j
@RestController
@RequestMapping("/seckill/activities")
public class SeckillActivityController {

    @Autowired
    private SeckillActivityService activityService;

    @PostMapping
    public Result<SeckillActivityVO> create(@Validated @RequestBody SeckillActivityCreateDTO dto) {
        return Result.success(activityService.create(dto));
    }

    @GetMapping
    public Result<List<SeckillActivityVO>> list(@RequestParam(required = false) Integer status) {
        return Result.success(activityService.list(status));
    }

    @GetMapping("/{id}")
    public Result<SeckillActivityVO> getById(@PathVariable Long id) {
        return Result.success(activityService.getById(id));
    }

    @PutMapping("/{id}")
    public Result<SeckillActivityVO> update(@PathVariable Long id,
                                            @Validated @RequestBody SeckillActivityUpdateDTO dto) {
        return Result.success(activityService.update(id, dto));
    }

    /**
     * 发布活动：DRAFT → ONGOING，同时预热库存到 Redis
     */
    @PostMapping("/{id}/publish")
    public Result<SeckillActivityVO> publish(@PathVariable Long id) {
        return Result.success(activityService.publish(id));
    }

    /**
     * 结束活动：ONGOING → ENDED
     */
    @PostMapping("/{id}/end")
    public Result<SeckillActivityVO> end(@PathVariable Long id) {
        return Result.success(activityService.end(id));
    }
}
