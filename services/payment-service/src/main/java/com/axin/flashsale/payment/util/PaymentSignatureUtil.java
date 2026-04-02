package com.axin.flashsale.payment.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * 支付签名工具类
 * 模拟支付宝 MD5 签名格式
 */
@Slf4j
@Component
public class PaymentSignatureUtil {

    @Value("${payment.signature.secret:default-secret-key}")
    private String signSecret;

    /**
     * 生成签名
     * 算法：将参数按 key 字母序排序，拼接成 key=value& 格式，追加 secret 后 MD5
     */
    public String generateSign(Map<String, String> params) {
        // 过滤空值和 sign 字段，按 key 排序
        TreeMap<String, String> sortedParams = new TreeMap<>(params);
        sortedParams.remove("sign");
        sortedParams.remove("signType");

        // 过滤空值
        String paramStr = sortedParams.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining("&"));

        // 追加 secret
        String signStr = paramStr + signSecret;

        // MD5
        return DigestUtils.md5DigestAsHex(signStr.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 验证签名
     */
    public boolean verifySign(Map<String, String> params, String expectedSign) {
        String calculatedSign = generateSign(params);
        boolean valid = calculatedSign.equals(expectedSign);
        if (!valid) {
            log.warn("签名验证失败, expected={}, calculated={}", expectedSign, calculatedSign);
        }
        return valid;
    }
}