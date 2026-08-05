package com.book.exception;

import com.book.util.Result;
import com.book.util.ResultCode;
import cn.dev33.satoken.exception.NotLoginException;
import cn.dev33.satoken.exception.NotRoleException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器：统一捕获异常并返回标准格式
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<Void> handleBusinessException(BusinessException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.fail(e.getCode(), e.getMessage());
    }

    /**
     * 参数校验异常（@RequestBody 校验失败）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<Void> handleValidException(MethodArgumentNotValidException e) {
        String msg = getFirstErrorMsg(e.getBindingResult().getFieldError());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * 参数绑定异常（表单/查询参数校验失败）
     */
    @ExceptionHandler(BindException.class)
    public Result<Void> handleBindException(BindException e) {
        String msg = getFirstErrorMsg(e.getBindingResult().getFieldError());
        return Result.fail(ResultCode.PARAM_ERROR.getCode(), msg);
    }

    /**
     * Sa-Token 未登录异常
     */
    @ExceptionHandler(NotLoginException.class)
    public Result<Void> handleNotLoginException(NotLoginException e) {
        return Result.fail(ResultCode.UNAUTHORIZED.getCode(), ResultCode.UNAUTHORIZED.getMsg());
    }

    /**
     * Sa-Token 无权限异常
     */
    @ExceptionHandler(NotRoleException.class)
    public Result<Void> handleNotRoleException(NotRoleException e) {
        return Result.fail(ResultCode.FORBIDDEN.getCode(), ResultCode.FORBIDDEN.getMsg());
    }

    /**
     * 兜底异常
     */
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.fail(ResultCode.ERROR.getCode(), "系统异常，请稍后重试");
    }

    private String getFirstErrorMsg(FieldError fieldError) {
        return fieldError == null ? ResultCode.PARAM_ERROR.getMsg() : fieldError.getDefaultMessage();
    }
}
