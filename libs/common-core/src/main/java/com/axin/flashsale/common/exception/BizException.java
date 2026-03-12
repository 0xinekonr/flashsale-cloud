package com.axin.flashsale.common.exception;

import lombok.Getter;

/**
 * 统一业务异常
 */
@Getter
public class BizException extends RuntimeException {
    private final Integer code;

    // 接收任何实现了 IResultCode 的枚举类
    public BizException(IResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}
