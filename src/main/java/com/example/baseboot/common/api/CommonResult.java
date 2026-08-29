package com.example.baseboot.common.api;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 统一 API 响应结果封装类
 *
 * @param <T> 响应数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommonResult<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 业务状态码
     */
    private long code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 响应时间戳
     */
    @Builder.Default
    private long timestamp = System.currentTimeMillis();

    /**
     * 成功返回结果
     */
    public static <T> CommonResult<T> success() {
        return success(null);
    }

    /**
     * 成功返回结果
     *
     * @param data 获取的数据
     */
    public static <T> CommonResult<T> success(T data) {
        return CommonResult.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(ResultCode.SUCCESS.getMessage())
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 成功返回结果
     *
     * @param data    获取的数据
     * @param message 提示信息
     */
    public static <T> CommonResult<T> success(T data, String message) {
        return CommonResult.<T>builder()
                .code(ResultCode.SUCCESS.getCode())
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     */
    public static <T> CommonResult<T> failed(ResultCode errorCode) {
        return CommonResult.<T>builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败返回结果
     *
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public static <T> CommonResult<T> failed(ResultCode errorCode, String message) {
        return CommonResult.<T>builder()
                .code(errorCode.getCode())
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 失败返回结果
     *
     * @param message 提示信息
     */
    public static <T> CommonResult<T> failed(String message) {
        return CommonResult.<T>builder()
                .code(ResultCode.FAILED.getCode())
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 参数验证失败返回结果
     */
    public static <T> CommonResult<T> validateFailed(String message) {
        return CommonResult.<T>builder()
                .code(ResultCode.VALIDATE_FAILED.getCode())
                .message(message)
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 未登录返回结果
     */
    public static <T> CommonResult<T> unauthorized() {
        return CommonResult.<T>builder()
                .code(ResultCode.UNAUTHORIZED.getCode())
                .message(ResultCode.UNAUTHORIZED.getMessage())
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 未授权返回结果
     */
    public static <T> CommonResult<T> forbidden() {
        return CommonResult.<T>builder()
                .code(ResultCode.FORBIDDEN.getCode())
                .message(ResultCode.FORBIDDEN.getMessage())
                .data(null)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}
