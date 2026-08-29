package com.example.baseboot.common.exception;

import com.example.baseboot.common.api.CommonResult;
import com.example.baseboot.common.api.ResultCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局统一异常拦截处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 业务异常处理
     */
    @ExceptionHandler(BusinessException.class)
    public CommonResult<Void> handleBusinessException(BusinessException e, HttpServletRequest request) {
        log.warn("业务异常 [URI: {}]: code={}, message={}", request.getRequestURI(), e.getCode(), e.getMessage());
        return CommonResult.<Void>builder()
                .code(e.getCode())
                .message(e.getMessage())
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 参数校验异常 (JSON Body @RequestBody @Valid)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public CommonResult<Void> handleValidException(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField() + " " + fieldError.getDefaultMessage();
            }
        }
        log.warn("参数校验异常: {}", message);
        return CommonResult.validateFailed(message != null ? message : ResultCode.VALIDATE_FAILED.getMessage());
    }

    /**
     * 参数绑定异常 (Form表单/Query参数)
     */
    @ExceptionHandler(BindException.class)
    public CommonResult<Void> handleBindException(BindException e) {
        BindingResult bindingResult = e.getBindingResult();
        String message = null;
        if (bindingResult.hasErrors()) {
            FieldError fieldError = bindingResult.getFieldError();
            if (fieldError != null) {
                message = fieldError.getField() + " " + fieldError.getDefaultMessage();
            }
        }
        log.warn("参数绑定异常: {}", message);
        return CommonResult.validateFailed(message != null ? message : ResultCode.VALIDATE_FAILED.getMessage());
    }

    /**
     * 非法参数异常
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public CommonResult<Void> handleIllegalArgumentException(IllegalArgumentException e) {
        log.warn("非法参数异常: {}", e.getMessage());
        return CommonResult.failed(ResultCode.VALIDATE_FAILED, e.getMessage());
    }

    /**
     * 系统未知未知异常兜底
     */
    @ExceptionHandler(Exception.class)
    public CommonResult<Void> handleException(Exception e, HttpServletRequest request) {
        log.error("系统内部异常 [URI: {}]: ", request.getRequestURI(), e);
        return CommonResult.failed("系统内部繁忙，请稍后再试: " + e.getMessage());
    }
}
