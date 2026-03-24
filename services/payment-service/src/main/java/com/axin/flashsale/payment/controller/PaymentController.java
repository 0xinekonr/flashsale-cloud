package com.axin.flashsale.payment.controller;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.payment.entity.Payment;
import com.axin.flashsale.payment.mapper.PaymentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostMapping
    public Result<Long> createPayment(@RequestBody Payment payment) {
        payment.setStatus("PENDING");
        payment.setCreateTime(LocalDateTime.now());
        paymentMapper.insert(payment);
        log.info("支付流水创建成功, paymentId={}", payment.getId());
        return Result.success(payment.getId());
    }

    @PostMapping("/{id}/notify")
    public Result<String> notifyPayment(@PathVariable Long id) {
        Payment payment = paymentMapper.selectById(id);
        if (payment == null || "SUCCESS".equals(payment.getStatus())) {
            return Result.success("已处理");
        }

        payment.setStatus("SUCCESS");
        payment.setTransactionId("ALIPAY_" + System.currentTimeMillis());
        paymentMapper.updateById(payment);

        rabbitTemplate.convertAndSend(GlobalConstants.MQ.ORDER_PAY_QUEUE, payment.getOrderId());
        log.info("支付成功, orderId={}, 已发送MQ通知", payment.getOrderId());
        return Result.success("SUCCESS");
    }
}
