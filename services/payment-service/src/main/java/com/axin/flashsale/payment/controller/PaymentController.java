package com.axin.flashsale.payment.controller;

import com.axin.flashsale.payment.client.OrderClient;
import com.axin.flashsale.payment.entity.Payment;
import com.axin.flashsale.payment.mapper.PaymentMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentMapper paymentMapper;

    @Autowired
    private OrderClient orderClient;

    /**
     * 1. 发起支付 （创建流水）
     */
    @PostMapping
    public Long createPayment(@RequestBody Payment payment){
        payment.setStatus("PENDING");
        payment.setCreateTime(LocalDateTime.now());
        paymentMapper.insert(payment);
        return payment.getId();
    }

    /**
     * 2. 模拟第三方回调 (Mock Callback)
     * 真实场景下，这是微信/支付宝服务器调用的接口
     */
    @PostMapping("/{id}/notify")
    public String notifyPayment(@PathVariable Long id){
        // A. 查询支付流水
        Payment payment = paymentMapper.selectById(id);
        if (payment == null || "SUCCESS".equals(payment.getStatus())){
            return "已处理";
        }

        // B. 修改支付状态
        payment.setStatus("SUCCESS");
        payment.setTransactionId("ALIPAY_" + System.currentTimeMillis());
        paymentMapper.updateById(payment);

        // C. 远程回调订单服务 （关键！闭环最后一步）
        // 在 M4 我们会把这一步改成发 MQ消息，现在先用 Feign 直连
        orderClient.finishOrder(payment.getOrderId());
        return "SUCCESS";
    }
}
