package com.axin.flashsale.seckill.service;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.dto.SeckillMessage;
import com.axin.flashsale.common.exception.BizException;
import com.axin.flashsale.seckill.dto.SeckillResultVO;
import com.axin.flashsale.seckill.entity.SeckillActivity;
import com.axin.flashsale.seckill.enums.SeckillActivityStatus;
import com.axin.flashsale.seckill.exception.SeckillErrorCode;
import com.axin.flashsale.seckill.mapper.SeckillActivityMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SeckillServiceTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private RabbitTemplate rabbitTemplate;

    @Mock
    private SeckillActivityMapper activityMapper;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private SeckillService seckillService;

    private static final Long ACTIVITY_ID = 1L;
    private static final Long USER_ID = 100L;
    private static final String STOCK_KEY = GlobalConstants.RedisKey.SECKILL_STOCK_PREFIX + ACTIVITY_ID;
    private static final String USER_KEY = GlobalConstants.RedisKey.SECKILL_USER_SET_PREFIX + ACTIVITY_ID;

    private SeckillActivity buildOngoingActivity() {
        SeckillActivity a = new SeckillActivity();
        a.setId(ACTIVITY_ID);
        a.setProductId(10L);
        a.setSeckillPrice(new BigDecimal("9.99"));
        a.setStatus(SeckillActivityStatus.ONGOING.getCode());
        a.setStartTime(LocalDateTime.now().minusHours(1));
        a.setEndTime(LocalDateTime.now().plusHours(1));
        a.setTotalStock(100);
        a.setAvailableStock(50);
        return a;
    }

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForSet()).thenReturn(setOperations);
    }

    @Nested
    class SeckillTests {

        @Test
        void activityNotFound_throwsException() {
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(null);

            assertThatThrownBy(() -> seckillService.seckill(ACTIVITY_ID, USER_ID))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(SeckillErrorCode.ACTIVITY_NOT_FOUND.getCode());
        }

        @Test
        void activityEnded_throwsException() {
            SeckillActivity activity = buildOngoingActivity();
            activity.setStatus(SeckillActivityStatus.ENDED.getCode());
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(activity);

            assertThatThrownBy(() -> seckillService.seckill(ACTIVITY_ID, USER_ID))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(SeckillErrorCode.ACTIVITY_ENDED.getCode());
        }

        @Test
        void activityDraft_throwsException() {
            SeckillActivity activity = buildOngoingActivity();
            activity.setStatus(SeckillActivityStatus.DRAFT.getCode());
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(activity);

            assertThatThrownBy(() -> seckillService.seckill(ACTIVITY_ID, USER_ID))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(SeckillErrorCode.ACTIVITY_DRAFT.getCode());
        }

        @Test
        void activityNotStartedYet_throwsException() {
            SeckillActivity activity = buildOngoingActivity();
            activity.setStartTime(LocalDateTime.now().plusHours(1));
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(activity);

            assertThatThrownBy(() -> seckillService.seckill(ACTIVITY_ID, USER_ID))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(SeckillErrorCode.ACTIVITY_NOT_START.getCode());
        }

        @Test
        void activityAlreadyEnded_timeWindow_throwsException() {
            SeckillActivity activity = buildOngoingActivity();
            activity.setEndTime(LocalDateTime.now().minusHours(1));
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(activity);

            assertThatThrownBy(() -> seckillService.seckill(ACTIVITY_ID, USER_ID))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(SeckillErrorCode.ACTIVITY_NOT_START.getCode());
        }

        @Test
        void repeatOrder_throwsException() {
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(buildOngoingActivity());
            when(setOperations.add(USER_KEY, USER_ID.toString())).thenReturn(0L);

            assertThatThrownBy(() -> seckillService.seckill(ACTIVITY_ID, USER_ID))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(SeckillErrorCode.REPEAT_ORDER.getCode());

            verify(redisTemplate, never()).execute(any(DefaultRedisScript.class), anyList(), anyString());
        }

        @Test
        void stockKeyNotFound_rollbacksUserSet() {
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(buildOngoingActivity());
            when(setOperations.add(USER_KEY, USER_ID.toString())).thenReturn(1L);
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString())).thenReturn(-1L);

            assertThatThrownBy(() -> seckillService.seckill(ACTIVITY_ID, USER_ID))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(SeckillErrorCode.ACTIVITY_NOT_START.getCode());

            verify(setOperations).remove(USER_KEY, USER_ID.toString());
        }

        @Test
        void stockEmpty_rollbacksUserSet() {
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(buildOngoingActivity());
            when(setOperations.add(USER_KEY, USER_ID.toString())).thenReturn(1L);
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString())).thenReturn(0L);

            assertThatThrownBy(() -> seckillService.seckill(ACTIVITY_ID, USER_ID))
                    .isInstanceOf(BizException.class)
                    .extracting("code").isEqualTo(SeckillErrorCode.STOCK_EMPTY.getCode());

            verify(setOperations).remove(USER_KEY, USER_ID.toString());
        }

        @Test
        void success_returnsTrue() {
            SeckillActivity activity = buildOngoingActivity();
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(activity);
            when(setOperations.add(USER_KEY, USER_ID.toString())).thenReturn(1L);
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString())).thenReturn(1L);

            boolean result = seckillService.seckill(ACTIVITY_ID, USER_ID);

            assertThat(result).isTrue();
            verify(activityMapper).deductStock(ACTIVITY_ID);
            verify(rabbitTemplate).convertAndSend(
                    eq(GlobalConstants.MQ.SECKILL_EXCHANGE),
                    eq(GlobalConstants.MQ.SECKILL_ROUTING_KEY),
                    any(SeckillMessage.class));
        }

        @Test
        void dbDeductFails_stillReturnsTrue() {
            SeckillActivity activity = buildOngoingActivity();
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(activity);
            when(setOperations.add(USER_KEY, USER_ID.toString())).thenReturn(1L);
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString())).thenReturn(1L);
            when(activityMapper.deductStock(ACTIVITY_ID)).thenThrow(new RuntimeException("DB error"));

            boolean result = seckillService.seckill(ACTIVITY_ID, USER_ID);

            assertThat(result).isTrue();
            verify(rabbitTemplate).convertAndSend(
                    eq(GlobalConstants.MQ.SECKILL_EXCHANGE),
                    eq(GlobalConstants.MQ.SECKILL_ROUTING_KEY),
                    any(SeckillMessage.class));
        }

        @Test
        void mqSendFails_rollbacksStockAndUserSet() {
            SeckillActivity activity = buildOngoingActivity();
            when(activityMapper.selectById(ACTIVITY_ID)).thenReturn(activity);
            when(setOperations.add(USER_KEY, USER_ID.toString())).thenReturn(1L);
            when(redisTemplate.execute(any(DefaultRedisScript.class), anyList(), anyString())).thenReturn(1L);
            when(redisTemplate.opsForValue()).thenReturn(valueOperations);

            // Mock RabbitTemplate to throw
            org.mockito.Mockito.doThrow(new RuntimeException("MQ unavailable"))
                    .when(rabbitTemplate).convertAndSend(anyString(), anyString(), any(Object.class));

            assertThatThrownBy(() -> seckillService.seckill(ACTIVITY_ID, USER_ID))
                    .isInstanceOf(BizException.class);

            verify(valueOperations).increment(STOCK_KEY);
            verify(setOperations).remove(USER_KEY, USER_ID.toString());
        }
    }

    @Nested
    class CheckResultTests {

        @Test
        void userParticipated_returnsTrue() {
            when(setOperations.isMember(USER_KEY, USER_ID.toString())).thenReturn(true);

            SeckillResultVO result = seckillService.checkResult(ACTIVITY_ID, USER_ID);

            assertThat(result.isParticipated()).isTrue();
            assertThat(result.getActivityId()).isEqualTo(ACTIVITY_ID);
        }

        @Test
        void userNotParticipated_returnsFalse() {
            when(setOperations.isMember(USER_KEY, USER_ID.toString())).thenReturn(false);

            SeckillResultVO result = seckillService.checkResult(ACTIVITY_ID, USER_ID);

            assertThat(result.isParticipated()).isFalse();
            assertThat(result.getActivityId()).isEqualTo(ACTIVITY_ID);
        }
    }
}
