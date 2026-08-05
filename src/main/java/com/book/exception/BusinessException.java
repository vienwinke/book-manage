package com.book.exception;

import com.book.util.ResultCode;
import lombok.Getter;

/**
 * 业务异常，业务逻辑错误时抛出，由全局异常处理器统一捕获
 */
@Getter
public class BusinessException extends RuntimeException {

    private final Integer code;

    public BusinessException(String msg) {
        super(msg);
        this.code = ResultCode.BUSINESS_ERROR.getCode();
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMsg());
        this.code = resultCode.getCode();
    }

    public BusinessException(Integer code, String msg) {
        super(msg);
        this.code = code;
    }
}
