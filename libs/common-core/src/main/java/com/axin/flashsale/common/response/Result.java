package com.axin.flashsale.common.response;

import com.axin.flashsale.common.constant.GlobalConstants;
import com.axin.flashsale.common.exception.IResultCode;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;

/**
 * 全局统一响应体
 */
@Data
public class Result<T> implements Serializable {
    private Integer code;
    private String message;
    private T data;
    private String traceId; // 核心：暴露链路追踪ID给前端

    protected Result() {}

    protected Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
        // Spring Boot 3 Micrometer 默认会将 traceId 注入到 MDC 中
        this.traceId = MDC.get("traceId");
    }

    // 成功（无返回数据）
    public static <T> Result<T> success() {
        return new Result<>(GlobalConstants.SUCCESS_CODE, GlobalConstants.SUCCESS_MESSAGE, null);
    }

    // 成功（有返回数据）
    public static <T> Result<T> success(T data) {
        return new Result<>(GlobalConstants.SUCCESS_CODE, GlobalConstants.SUCCESS_MESSAGE, data);
    }

    // 失败（基于通用枚举/业务枚举）
    public static <T> Result<T> fail(IResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), null);
    }

    // 失败（自定义 code 和 message）
    public static <T> Result<T> fail(Integer code, String message) {
        return new Result<>(code, message, null);
    }

}
