package com.axin.flashsale.payment.controller;

import com.axin.flashsale.payment.client.OrderClient;
import com.axin.flashsale.payment.entity.Payment;
import com.axin.flashsale.payment.mapper.PaymentMapper;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentMapper paymentMapper;

//    @Autowired
//    private OrderClient orderClient;  // M4: 移除 Feign Client

    @Autowired
    private RabbitTemplate rabbitTemplate;  // M4: 注入 RabbitMQ 模板

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

        // C. 发送 MQ 消息 （异步解耦）
        // 交换机(Exchange): "" (使用默认交换机)
        // 路由键(Routing Key): "order.pay.queue" (直接发给这个队列)
        // 消息内容：订单ID (实际生产通常发 JSON 对象，这里简单发个ID)
        rabbitTemplate.convertAndSend("order.pay.queue", payment.getOrderId());
        return "SUCCESS";
    }
}
