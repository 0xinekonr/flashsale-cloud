package com.axin.flashsale.common.web.handler;

import com.axin.flashsale.common.exception.BizException;
import com.axin.flashsale.common.exception.SystemCode;
import com.axin.flashsale.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

/**
 * 全局统一异常拦截器
 * 进作用于引入了 common-web 的 Spring Web MVC 微服务
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 1. 拦截业务异常 (我们主动 throw 的 BizException)
     */
    @ExceptionHandler(BizException.class)
    public Result<Void> handleBizException(BizException e) {
        // 业务异常通常是正常逻辑的阻断（如库存不足），记 warn 级别即可
        log.warn("业务拦截: [{}] {}", e.getCode(), e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 2. 拦截参数校验异常 (@Validated 失败时抛出的异常)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<Void> handleValidationException(Exception e) {
        String errorMsg = "参数错误";
        if (e instanceof MethodArgumentNotValidException ex) {
            // 将多个参数校验失败的信息拼装起来，例如: "userId不能为空, productId必须大于0"
            errorMsg = ex.getBindingResult().getFieldErrors().stream()
                    .map(FieldError::getDefaultMessage)
                    .collect(Collectors.joining(", "));
        }
        log.warn("参数校验失败: {}", errorMsg);
        return Result.fail(SystemCode.PARAM_ERROR.getCode(), errorMsg);
    }

    /**
     * 3. 兜底拦截未知异常 (如 NullPointerException, 数据库断连等)
     * 绝不把包含代码堆栈的 500 页面直接暴露给前端/黑客
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        // 未知异常必须记 error 并打印完整堆栈，以便排查
        log.error("系统未知致命异常: ", e);
        return Result.fail(SystemCode.SYSTEM_ERROR);
    }
}