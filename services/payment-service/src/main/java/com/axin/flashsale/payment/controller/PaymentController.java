package com.axin.flashsale.payment.controller;

import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.payment.dto.PaymentCallbackDTO;
import com.axin.flashsale.payment.dto.PaymentRequest;
import com.axin.flashsale.payment.dto.RefundRequest;
import com.axin.flashsale.payment.entity.Payment;
import com.axin.flashsale.payment.service.PaymentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping
    public Result<Long> createPayment(@RequestBody PaymentRequest request) {
        Long paymentId = paymentService.createPayment(
                request.getOrderId(),
                request.getUserId(),
                request.getAmount()
        );
        return Result.success(paymentId);
    }

    /**
     * 支付回调接口（模拟第三方支付平台回调）
     * 需要验证签名
     */
    @PostMapping("/callback")
    public Result<String> paymentCallback(@RequestBody PaymentCallbackDTO callback) {
        paymentService.processCallback(callback);
        return Result.success("SUCCESS");
    }

    /**
     * 模拟回调接口（仅用于测试，无需签名）
     */
    @PostMapping("/{id}/notify")
    public Result<String> notifyPayment(@PathVariable Long id) {
        // 构造模拟回调数据
        PaymentCallbackDTO callback = new PaymentCallbackDTO();
        callback.setPaymentId(id);
        callback.setTransactionId("ALIPAY_" + System.currentTimeMillis());
        callback.setStatus("SUCCESS");
        callback.setTimestamp(System.currentTimeMillis());
        // 模拟回调不验证签名，直接处理
        paymentService.processCallbackWithoutSignVerify(id, callback.getTransactionId());
        return Result.success("SUCCESS");
    }

    @PostMapping("/{id}/refund")
    public Result<Void> refundPayment(@PathVariable Long id,
                                       @RequestBody(required = false) RefundRequest request) {
        paymentService.refund(id, request != null ? request.getAmount() : null);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<Payment> getPayment(@PathVariable Long id) {
        return Result.success(paymentService.getById(id));
    }
}
