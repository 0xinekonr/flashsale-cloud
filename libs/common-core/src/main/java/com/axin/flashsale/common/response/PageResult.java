package com.axin.flashsale.common.response;

import com.axin.flashsale.common.constant.GlobalConstants;
import lombok.Data;
import org.slf4j.MDC;

import java.io.Serializable;
import java.util.List;

@Data
public class PageResult<T> implements Serializable {
    private Integer code;
    private String message;
    private List<T> data;
    private Long total;
    private Integer page;
    private Integer size;
    private String traceId;

    protected PageResult() {}

    protected PageResult(Integer code, String message, List<T> data, Long total, Integer page, Integer size) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.total = total;
        this.page = page;
        this.size = size;
        this.traceId = MDC.get("traceId");
    }

    public static <T> PageResult<T> success(List<T> data, Long total, Integer page, Integer size) {
        return new PageResult<>(GlobalConstants.SUCCESS_CODE, GlobalConstants.SUCCESS_MESSAGE, data, total, page, size);
    }
}
