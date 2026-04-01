package com.axin.flashsale.payment.controller;

import com.axin.flashsale.common.response.Result;
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

    @PostMapping("/{id}/notify")
    public Result<String> notifyPayment(@PathVariable Long id) {
        String transactionId = "ALIPAY_" + System.currentTimeMillis();
        paymentService.processCallback(id, transactionId);
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
