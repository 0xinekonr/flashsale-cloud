package com.axin.flashsale.payment.client.fallback;

import com.axin.flashsale.common.response.Result;
import com.axin.flashsale.payment.client.OrderClient;
import com.axin.flashsale.payment.dto.OrderDTO;
import com.axin.flashsale.payment.exception.PaymentErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderClientFallback implements OrderClient {

    @Override
    public Result<OrderDTO> getOrder(Long orderId) {
        log.error("OrderClient 调用失败, orderId={}", orderId);
        return Result.fail(PaymentErrorCode.ORDER_SERVICE_UNAVAILABLE);
    }
}
