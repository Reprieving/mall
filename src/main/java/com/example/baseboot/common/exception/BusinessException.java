package com.example.baseboot.common.exception;

import com.example.baseboot.common.api.ResultCode;
import lombok.Getter;

/**
 * 自定义业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final long code;
    private final String message;

    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAILED.getCode();
        this.message = message;
    }

    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
    }

    public BusinessException(ResultCode resultCode, String message) {
        super(message);
        this.code = resultCode.getCode();
        this.message = message;
    }

    public BusinessException(long code, String message) {
        super(message);
        this.code = code;
        this.message = message;
    }
}
