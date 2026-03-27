package com.axin.flashsale.seckill.service;

import com.axin.flashsale.common.exception.BizException;
import com.axin.flashsale.common.exception.SystemCode;
import com.axin.flashsale.seckill.component.SeckillLoader;
import com.axin.flashsale.seckill.dto.SeckillActivityCreateDTO;
import com.axin.flashsale.seckill.dto.SeckillActivityUpdateDTO;
import com.axin.flashsale.seckill.dto.SeckillActivityVO;
import com.axin.flashsale.seckill.entity.SeckillActivity;
import com.axin.flashsale.seckill.enums.SeckillActivityStatus;
import com.axin.flashsale.seckill.exception.SeckillErrorCode;
import com.axin.flashsale.seckill.mapper.SeckillActivityMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 秒杀活动管理服务
 *
 * 负责活动的 CRUD 操作和状态流转。
 * 状态机: DRAFT(草稿) → ONGOING(进行中) → ENDED(已结束)
 *
 * - DRAFT: 管理员编辑中，可修改所有字段
 * - ONGOING: 活动进行中，库存已预热到 Redis，不可修改，允许秒杀
 * - ENDED: 活动已结束，不可修改，不可秒杀
 */
@Slf4j
@Service
public class SeckillActivityService {

    @Autowired
    private SeckillActivityMapper activityMapper;

    @Autowired
    private SeckillLoader seckillLoader;

    /**
     * 创建活动（默认 DRAFT 状态）
     */
    public SeckillActivityVO create(SeckillActivityCreateDTO dto) {
        SeckillActivity activity = new SeckillActivity();
        activity.setActivityName(dto.getActivityName());
        activity.setProductId(dto.getProductId());
        activity.setStartTime(dto.getStartTime());
        activity.setEndTime(dto.getEndTime());
        activity.setTotalStock(dto.getTotalStock());
        activity.setAvailableStock(dto.getTotalStock());
        activity.setSeckillPrice(dto.getSeckillPrice());
        activity.setStatus(SeckillActivityStatus.DRAFT.getCode());

        activityMapper.insert(activity);
        log.info("创建秒杀活动: id={}, name={}", activity.getId(), activity.getActivityName());
        return toVO(activity);
    }

    /**
     * 更新活动（仅 DRAFT 状态可编辑）
     *
     * 如果修改了 totalStock，同步更新 availableStock
     */
    @Transactional(rollbackFor = Exception.class)
    public SeckillActivityVO update(Long id, SeckillActivityUpdateDTO dto) {
        SeckillActivity activity = getActivityOrThrow(id);

        if (activity.getStatus() != SeckillActivityStatus.DRAFT.getCode()) {
            throw new BizException(SystemCode.FORBIDDEN, "仅草稿状态的活动可以编辑");
        }

        if (dto.getActivityName() != null) {
            activity.setActivityName(dto.getActivityName());
        }
        if (dto.getProductId() != null) {
            activity.setProductId(dto.getProductId());
        }
        if (dto.getStartTime() != null) {
            activity.setStartTime(dto.getStartTime());
        }
        if (dto.getEndTime() != null) {
            activity.setEndTime(dto.getEndTime());
        }
        if (dto.getTotalStock() != null) {
            activity.setTotalStock(dto.getTotalStock());
            // 草稿状态下 availableStock = totalStock
            activity.setAvailableStock(dto.getTotalStock());
        }
        if (dto.getSeckillPrice() != null) {
            activity.setSeckillPrice(dto.getSeckillPrice());
        }

        activityMapper.updateById(activity);
        log.info("更新秒杀活动: id={}", id);
        return toVO(activity);
    }

    /**
     * 发布活动（DRAFT → ONGOING）
     *
     * 状态变更为进行中，同时触发库存预热到 Redis。
     * 预热后 Lua 脚本才能对该活动进行原子库存扣减。
     */
    @Transactional(rollbackFor = Exception.class)
    public SeckillActivityVO publish(Long id) {
        SeckillActivity activity = getActivityOrThrow(id);

        if (activity.getStatus() != SeckillActivityStatus.DRAFT.getCode()) {
            throw new BizException(SeckillErrorCode.ACTIVITY_DRAFT);
        }

        activity.setStatus(SeckillActivityStatus.ONGOING.getCode());
        activityMapper.updateById(activity);

        // 发布后立即预热库存到 Redis
        seckillLoader.preloadActivity(id);
        log.info("发布秒杀活动: id={}, stock={}", id, activity.getAvailableStock());
        return toVO(activity);
    }

    /**
     * 结束活动（ONGOING → ENDED）
     */
    public SeckillActivityVO end(Long id) {
        SeckillActivity activity = getActivityOrThrow(id);

        if (activity.getStatus() != SeckillActivityStatus.ONGOING.getCode()) {
            throw new BizException(SeckillErrorCode.ACTIVITY_ENDED);
        }

        activity.setStatus(SeckillActivityStatus.ENDED.getCode());
        activityMapper.updateById(activity);
        log.info("结束秒杀活动: id={}", id);
        return toVO(activity);
    }

    /**
     * 查询活动详情
     */
    public SeckillActivityVO getById(Long id) {
        return toVO(getActivityOrThrow(id));
    }

    /**
     * 查询活动列表（可按状态过滤）
     */
    public List<SeckillActivityVO> list(Integer status) {
        LambdaQueryWrapper<SeckillActivity> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(SeckillActivity::getStatus, status);
        }
        wrapper.orderByDesc(SeckillActivity::getCreateTime);

        return activityMapper.selectList(wrapper).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    private SeckillActivity getActivityOrThrow(Long id) {
        SeckillActivity activity = activityMapper.selectById(id);
        if (activity == null) {
            throw new BizException(SeckillErrorCode.ACTIVITY_NOT_FOUND);
        }
        return activity;
    }

    private SeckillActivityVO toVO(SeckillActivity activity) {
        SeckillActivityVO vo = new SeckillActivityVO();
        vo.setId(activity.getId());
        vo.setActivityName(activity.getActivityName());
        vo.setProductId(activity.getProductId());
        vo.setStartTime(activity.getStartTime());
        vo.setEndTime(activity.getEndTime());
        vo.setTotalStock(activity.getTotalStock());
        vo.setAvailableStock(activity.getAvailableStock());
        vo.setSeckillPrice(activity.getSeckillPrice());
        vo.setStatus(activity.getStatus());
        vo.setCreateTime(activity.getCreateTime());
        vo.setUpdateTime(activity.getUpdateTime());
        return vo;
    }
}
