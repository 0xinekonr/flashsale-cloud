package com.axin.flashsale.payment.component;

import com.axin.flashsale.common.redis.lock.DistributedLock;
import com.axin.flashsale.payment.client.OrderClient;
import com.axin.flashsale.payment.dto.OrderDTO;
import com.axin.flashsale.payment.entity.Payment;
import com.axin.flashsale.payment.mapper.PaymentMapper;
import com.axin.flashsale.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 支付-订单对账任务
 * 定期检查支付成功但订单状态不一致的情况
 */
@Slf4j
@Component
public class PaymentOrderReconciler {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private DistributedLock distributedLock;

    private static final String LOCK_KEY = "reconcile:payment-order";
    private static final int CHECK_HOURS = 24;

    /**
     * 每 5 分钟执行一次对账
     */
    @Scheduled(cron = "0 */5 * * * *")
    public void reconcile() {
        distributedLock.tryLock(LOCK_KEY, 0, 60, TimeUnit.SECONDS, () -> {
            doReconcile();
        });
    }

    private void doReconcile() {
        List<Payment> payments = paymentMapper.findRecentSuccessPayments(CHECK_HOURS);
        if (payments.isEmpty()) {
            return;
        }

        log.info("开始对账, 检查 {} 笔支付记录", payments.size());

        int matchCount = 0;
        int mismatchCount = 0;

        for (Payment payment : payments) {
            try {
                Result<OrderDTO> result = orderClient.getOrder(payment.getOrderId());
                if (result == null || result.getData() == null) {
                    log.error("对账异常: 订单不存在, paymentId={}, orderId={}",
                            payment.getId(), payment.getOrderId());
                    mismatchCount++;
                    continue;
                }

                OrderDTO order = result.getData();
                // 订单状态应为 PAID(1)
                if (!Integer.valueOf(1).equals(order.getStatus())) {
                    log.error("对账发现不一致: paymentId={}, orderId={}, paymentStatus={}, orderStatus={}",
                            payment.getId(), payment.getOrderId(), payment.getStatus(), order.getStatus());
                    mismatchCount++;
                } else {
                    matchCount++;
                }
            } catch (Exception e) {
                log.error("对账查询订单失败, paymentId={}, orderId={}",
                        payment.getId(), payment.getOrderId(), e);
            }
        }

        if (mismatchCount > 0) {
            log.warn("对账完成: 总数={}, 一致={}, 不一致={}", payments.size(), matchCount, mismatchCount);
        } else {
            log.info("对账完成: 总数={}, 全部一致", payments.size());
        }
    }
}