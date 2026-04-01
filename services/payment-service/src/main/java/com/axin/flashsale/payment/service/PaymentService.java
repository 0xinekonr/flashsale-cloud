package com.axin.flashsale.payment.service;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.exception.BizException;
import com.axin.flashsale.payment.client.OrderClient;
import com.axin.flashsale.payment.dto.OrderDTO;
import com.axin.flashsale.payment.entity.Payment;
import com.axin.flashsale.payment.enums.PaymentStatusEnum;
import com.axin.flashsale.payment.exception.PaymentErrorCode;
import com.axin.flashsale.payment.mapper.PaymentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 支付服务
 */
@Slf4j
@Service
public class PaymentService {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderClient orderClient;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * 创建支付流水
     * 校验订单状态后创建支付记录
     */
    @Transactional
    public Long createPayment(Long orderId, Long userId, BigDecimal amount) {
        // 1. 校验订单状态
        OrderDTO order = validateOrderForPayment(orderId);

        // 2. 创建支付流水
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setUserId(userId != null ? userId : order.getUserId());
        payment.setAmount(amount);
        payment.setStatus(PaymentStatusEnum.PENDING.getCode());
        payment.setCreateTime(LocalDateTime.now());

        paymentMapper.insert(payment);
        log.info("支付流水创建成功, paymentId={}, orderId={}", payment.getId(), orderId);
        return payment.getId();
    }

    /**
     * 处理支付回调（模拟）
     * 后续 commit 会添加签名验证和幂等处理
     */
    @Transactional
    public void processCallback(Long paymentId, String transactionId) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BizException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        if (PaymentStatusEnum.SUCCESS.getCode().equals(payment.getStatus())) {
            log.info("支付已处理, paymentId={}", paymentId);
            return;
        }

        // 更新支付状态
        payment.setStatus(PaymentStatusEnum.SUCCESS.getCode());
        payment.setTransactionId(transactionId);
        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.updateById(payment);

        // 发送 MQ 通知订单服务
        rabbitTemplate.convertAndSend(GlobalConstants.MQ.ORDER_PAY_QUEUE, payment.getOrderId());
        log.info("支付成功, paymentId={}, orderId={}, 已发送MQ通知", paymentId, payment.getOrderId());
    }

    /**
     * 退款
     * 仅 SUCCESS 状态可退款
     */
    @Transactional
    public void refund(Long paymentId, BigDecimal amount) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BizException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }

        if (!PaymentStatusEnum.SUCCESS.getCode().equals(payment.getStatus())) {
            throw new BizException(PaymentErrorCode.REFUND_NOT_ALLOWED);
        }

        // 计算可退金额
        BigDecimal refunded = payment.getRefundAmount() != null ? payment.getRefundAmount() : BigDecimal.ZERO;
        BigDecimal maxRefundable = payment.getAmount().subtract(refunded);

        if (amount == null) {
            amount = maxRefundable; // 默认全额退款
        }

        if (amount.compareTo(BigDecimal.ZERO) <= 0 || amount.compareTo(maxRefundable) > 0) {
            throw new BizException(PaymentErrorCode.REFUND_NOT_ALLOWED, "退款金额无效");
        }

        // 更新退款信息
        payment.setRefundAmount(refunded.add(amount));
        payment.setRefundTime(LocalDateTime.now());

        // 如果全部退完，更新状态为 REFUNDED
        if (payment.getRefundAmount().compareTo(payment.getAmount()) >= 0) {
            payment.setStatus(PaymentStatusEnum.REFUNDED.getCode());
        }

        payment.setUpdateTime(LocalDateTime.now());
        paymentMapper.updateById(payment);
        log.info("退款成功, paymentId={}, refundAmount={}, totalRefunded={}",
                paymentId, amount, payment.getRefundAmount());
    }

    /**
     * 根据 ID 查询支付记录
     */
    public Payment getById(Long paymentId) {
        Payment payment = paymentMapper.selectById(paymentId);
        if (payment == null) {
            throw new BizException(PaymentErrorCode.PAYMENT_NOT_FOUND);
        }
        return payment;
    }

    /**
     * 校验订单是否可支付
     */
    private OrderDTO validateOrderForPayment(Long orderId) {
        var result = orderClient.getOrder(orderId);
        if (result == null || result.getData() == null) {
            throw new BizException(PaymentErrorCode.ORDER_NOT_FOUND);
        }

        OrderDTO order = result.getData();
        if (!Integer.valueOf(0).equals(order.getStatus())) { // NEW = 0
            throw new BizException(PaymentErrorCode.ORDER_STATUS_INVALID);
        }

        return order;
    }
}
