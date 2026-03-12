package com.axin.flashsale.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 全局通用系统响应码
 */
@Getter
@AllArgsConstructor
public enum SystemCode implements IResultCode{

    SYSTEM_ERROR(500, "系统内部异常，请稍后再试"),
    PARAM_ERROR(400, "参数校验失败"),
    UNAUTHORIZED(401, "未登录或Token已过期"),
    FORBIDDEN(403, "无权限访问此资源"),

    // 服务降级/限流
    FLOW_LIMITED(429, "当前系统繁忙，请稍后再试");

    private final Integer code;
    private final String message;
}
