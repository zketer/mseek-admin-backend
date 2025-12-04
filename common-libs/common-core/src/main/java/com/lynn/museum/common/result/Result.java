package com.lynn.museum.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.io.Serializable;

/**
 * 通用响应结果类
 * @param <T> 数据类型
 * @author lynn
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result<T> implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 响应消息（中文）
     */
    private String message;
    
    /**
     * 响应消息（英文）
     */
    private String messageEn;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 时间戳
     */
    private Long timestamp;
    
    /**
     * 请求ID（用于链路追踪）
     */
    private String requestId;
    
    // ==================== 成功响应方法 ====================
    
    /**
     * 成功响应（无数据）
     */
    public static <T> Result<T> success() {
        return success(null);
    }
    
    /**
     * 成功响应（带数据）
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = ResultCode.SUCCESS.getMessage();
        result.messageEn = ResultCode.SUCCESS.getMessageEn();
        result.data = data;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    /**
     * 成功响应（自定义消息）
     */
    public static <T> Result<T> success(T data, String message) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = message;
        result.messageEn = message;
        result.data = data;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    /**
     * 成功响应（自定义中英文消息）
     */
    public static <T> Result<T> success(T data, String message, String messageEn) {
        Result<T> result = new Result<>();
        result.code = ResultCode.SUCCESS.getCode();
        result.message = message;
        result.messageEn = messageEn;
        result.data = data;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    // ==================== 错误响应方法 ====================
    
    /**
     * 错误响应（使用ResultCode枚举）
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.code = resultCode.getCode();
        result.message = resultCode.getMessage();
        result.messageEn = resultCode.getMessageEn();
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    /**
     * 错误响应（使用ResultCode枚举 + 自定义数据）
     */
    public static <T> Result<T> error(ResultCode resultCode, T data) {
        Result<T> result = new Result<>();
        result.code = resultCode.getCode();
        result.message = resultCode.getMessage();
        result.messageEn = resultCode.getMessageEn();
        result.data = data;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    /**
     * 错误响应（默认500错误码）
     */
    public static <T> Result<T> error(String message) {
        Result<T> result = new Result<>();
        result.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        result.message = message;
        result.messageEn = message;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    /**
     * 错误响应（默认500错误码 + 中英文消息）
     */
    public static <T> Result<T> error(String message, String messageEn) {
        Result<T> result = new Result<>();
        result.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        result.message = message;
        result.messageEn = messageEn;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    /**
     * 错误响应（自定义错误码）
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.messageEn = message;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    /**
     * 错误响应（自定义错误码 + 中英文消息）
     */
    public static <T> Result<T> error(Integer code, String message, String messageEn) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.messageEn = messageEn;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    /**
     * 错误响应（自定义错误码 + 中英文消息 + 数据）
     */
    public static <T> Result<T> error(Integer code, String message, String messageEn, T data) {
        Result<T> result = new Result<>();
        result.code = code;
        result.message = message;
        result.messageEn = messageEn;
        result.data = data;
        result.timestamp = System.currentTimeMillis();
        return result;
    }
    
    // ==================== 常用HTTP状态码快捷方法 ====================
    
    /**
     * 400 Bad Request
     */
    public static <T> Result<T> badRequest() {
        return error(ResultCode.BAD_REQUEST);
    }
    
    /**
     * 400 Bad Request（自定义消息）
     */
    public static <T> Result<T> badRequest(String message) {
        return error(ResultCode.BAD_REQUEST.getCode(), message);
    }
    
    /**
     * 401 Unauthorized
     */
    public static <T> Result<T> unauthorized() {
        return error(ResultCode.UNAUTHORIZED);
    }
    
    /**
     * 403 Forbidden
     */
    public static <T> Result<T> forbidden() {
        return error(ResultCode.FORBIDDEN);
    }
    
    /**
     * 404 Not Found
     */
    public static <T> Result<T> notFound() {
        return error(ResultCode.NOT_FOUND);
    }
    
    /**
     * 404 Not Found（自定义消息）
     */
    public static <T> Result<T> notFound(String message) {
        return error(ResultCode.NOT_FOUND.getCode(), message);
    }
    
    /**
     * 429 Too Many Requests
     */
    public static <T> Result<T> tooManyRequests() {
        return error(ResultCode.TOO_MANY_REQUESTS);
    }
    
    /**
     * 500 Internal Server Error
     */
    public static <T> Result<T> internalServerError() {
        return error(ResultCode.INTERNAL_SERVER_ERROR);
    }
    
    /**
     * 503 Service Unavailable
     */
    public static <T> Result<T> serviceUnavailable() {
        return error(ResultCode.SERVICE_UNAVAILABLE);
    }
    
    // ==================== 实用判断方法 ====================
    
    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }
    
    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }
    
    /**
     * 判断是否为指定错误码
     */
    public boolean isError(ResultCode resultCode) {
        return resultCode.getCode().equals(this.code);
    }
    
    /**
     * 判断是否为指定错误码
     */
    public boolean isError(Integer code) {
        return code.equals(this.code);
    }
    
    // ==================== 链式调用方法 ====================
    
    /**
     * 设置请求ID（用于链路追踪）
     */
    public Result<T> requestId(String requestId) {
        this.requestId = requestId;
        return this;
    }
    
    /**
     * 设置时间戳
     */
    public Result<T> timestamp(Long timestamp) {
        this.timestamp = timestamp;
        return this;
    }
    
    /**
     * 设置当前时间戳
     */
    public Result<T> currentTimestamp() {
        this.timestamp = System.currentTimeMillis();
        return this;
    }
}