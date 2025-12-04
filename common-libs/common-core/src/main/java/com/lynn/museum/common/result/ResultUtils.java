package com.lynn.museum.common.result;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 响应结果工具类
 * 提供便捷的响应构建方法，用于统一响应格式
 * 
 * @author lynn
 * @since 1.0.0
 */
public class ResultUtils {
    
    // ==================== 成功响应方法 ====================
    
    /**
     * 构建成功响应（无数据）
     * 
     * @return 成功响应
     */
    public static <T> Result<T> success() {
        return Result.success();
    }
    
    /**
     * 构建成功响应（带数据）
     * 
     * @param data 响应数据
     * @return 成功响应
     */
    public static <T> Result<T> success(T data) {
        return Result.success(data);
    }
    
    /**
     * 构建成功响应（带自定义消息）
     * 
     * @param message 成功消息
     * @return 成功响应
     */
    @SuppressWarnings("unchecked")
    public static <T> Result<T> success(String message) {
        return (Result<T>) Result.success(message);
    }
    
    /**
     * 构建成功响应（带数据和自定义消息）
     * 
     * @param data 响应数据
     * @param message 成功消息
     * @return 成功响应
     */
    public static <T> Result<T> success(T data, String message) {
        return Result.success(data, message);
    }
    
    /**
     * 构建成功响应（带数据和双语消息）
     * 
     * @param data 响应数据
     * @param message 成功消息（中文）
     * @param messageEn 成功消息（英文）
     * @return 成功响应
     */
    public static <T> Result<T> success(T data, String message, String messageEn) {
        return Result.success(data, message, messageEn);
    }
    
    // ==================== 失败响应方法 ====================
    
    /**
     * 构建失败响应
     * 
     * @param resultCode 错误码枚举
     * @return 失败响应
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return Result.error(resultCode);
    }
    
    /**
     * 构建失败响应（带自定义消息）
     * 
     * @param resultCode 错误码枚举
     * @param message 自定义错误消息
     * @return 失败响应
     */
    @SuppressWarnings("unchecked")
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return (Result<T>) Result.error(resultCode, message);
    }
    
    /**
     * 构建失败响应（带双语消息）
     * 
     * @param resultCode 错误码枚举
     * @param message 错误消息（中文）
     * @param messageEn 错误消息（英文）
     * @return 失败响应
     */
    public static <T> Result<T> error(ResultCode resultCode, String message, String messageEn) {
        return Result.error(resultCode.getCode(), message, messageEn);
    }
    
    /**
     * 构建失败响应
     * 
     * @param code 错误码
     * @param message 错误消息
     * @return 失败响应
     */
    public static <T> Result<T> error(int code, String message) {
        return Result.error(code, message);
    }
    
    /**
     * 构建失败响应（带双语消息）
     * 
     * @param code 错误码
     * @param message 错误消息（中文）
     * @param messageEn 错误消息（英文）
     * @return 失败响应
     */
    public static <T> Result<T> error(int code, String message, String messageEn) {
        return Result.error(code, message, messageEn);
    }
    
    /**
     * 构建失败响应（带数据）
     * 
     * @param resultCode 错误码枚举
     * @param data 附加数据
     * @return 失败响应
     */
    public static <T> Result<T> error(ResultCode resultCode, T data) {
        return Result.error(resultCode.getCode(), resultCode.getMessage(), resultCode.getMessageEn(), data);
    }
    
    /**
     * 构建失败响应（带数据和自定义消息）
     * 
     * @param resultCode 错误码枚举
     * @param message 自定义错误消息
     * @param data 附加数据
     * @return 失败响应
     */
    public static <T> Result<T> error(ResultCode resultCode, String message, T data) {
        return Result.error(resultCode.getCode(), message, resultCode.getMessageEn(), data);
    }
    
    // ==================== HTTP状态码快捷方法 ====================
    
    /**
     * 400 Bad Request
     * 
     * @param message 错误消息
     * @return 失败响应
     */
    public static <T> Result<T> badRequest(String message) {
        return Result.badRequest(message);
    }
    
    /**
     * 401 Unauthorized
     * 
     * @return 失败响应
     */
    public static <T> Result<T> unauthorized() {
        return Result.unauthorized();
    }
    
    /**
     * 401 Unauthorized
     * 
     * @param message 错误消息
     * @return 失败响应
     */
    public static <T> Result<T> unauthorized(String message) {
        return Result.error(401, message, "Unauthorized");
    }
    
    /**
     * 403 Forbidden
     * 
     * @return 失败响应
     */
    public static <T> Result<T> forbidden() {
        return Result.forbidden();
    }
    
    /**
     * 403 Forbidden
     * 
     * @param message 错误消息
     * @return 失败响应
     */
    public static <T> Result<T> forbidden(String message) {
        return Result.error(403, message, "Forbidden");
    }
    
    /**
     * 404 Not Found
     * 
     * @return 失败响应
     */
    public static <T> Result<T> notFound() {
        return Result.notFound();
    }
    
    /**
     * 404 Not Found
     * 
     * @param message 错误消息
     * @return 失败响应
     */
    public static <T> Result<T> notFound(String message) {
        return Result.notFound(message);
    }
    
    /**
     * 500 Internal Server Error
     * 
     * @return 失败响应
     */
    public static <T> Result<T> internalServerError() {
        return Result.internalServerError();
    }
    
    /**
     * 500 Internal Server Error
     * 
     * @param message 错误消息
     * @return 失败响应
     */
    public static <T> Result<T> internalServerError(String message) {
        return Result.error(500, message, "Internal Server Error");
    }
    
    // ==================== 条件响应方法 ====================
    
    /**
     * 根据条件返回成功或失败响应
     * 
     * @param condition 条件
     * @param successData 成功时的数据
     * @param errorCode 失败时的错误码
     * @return 响应结果
     */
    public static <T> Result<T> condition(boolean condition, T successData, ResultCode errorCode) {
        return condition ? success(successData) : error(errorCode);
    }
    
    /**
     * 根据条件返回成功或失败响应
     * 
     * @param condition 条件
     * @param successData 成功时的数据
     * @param errorCode 失败时的错误码
     * @param errorMessage 失败时的错误消息
     * @return 响应结果
     */
    public static <T> Result<T> condition(boolean condition, T successData, ResultCode errorCode, String errorMessage) {
        return condition ? success(successData) : error(errorCode, errorMessage);
    }
    
    /**
     * 根据数据是否为null返回响应
     * 
     * @param data 数据
     * @param errorCode 数据为null时的错误码
     * @return 响应结果
     */
    public static <T> Result<T> ofNullable(T data, ResultCode errorCode) {
        return data != null ? success(data) : error(errorCode);
    }
    
    /**
     * 根据数据是否为null返回响应
     * 
     * @param data 数据
     * @param errorCode 数据为null时的错误码
     * @param errorMessage 数据为null时的错误消息
     * @return 响应结果
     */
    public static <T> Result<T> ofNullable(T data, ResultCode errorCode, String errorMessage) {
        return data != null ? success(data) : error(errorCode, errorMessage);
    }
    
    // ==================== 响应构建器 ====================
    
    /**
     * 创建响应构建器
     * 
     * @return 响应构建器
     */
    public static <T> ResultBuilder<T> builder() {
        return new ResultBuilder<>();
    }
    
    /**
     * 响应构建器
     */
    public static class ResultBuilder<T> {
        private int code;
        private String message;
        private String messageEn;
        private T data;
        private LocalDateTime timestamp;
        private String requestId;
        
        public ResultBuilder<T> code(int code) {
            this.code = code;
            return this;
        }
        
        public ResultBuilder<T> code(ResultCode resultCode) {
            this.code = resultCode.getCode();
            this.message = resultCode.getMessage();
            this.messageEn = resultCode.getMessageEn();
            return this;
        }
        
        public ResultBuilder<T> message(String message) {
            this.message = message;
            return this;
        }
        
        public ResultBuilder<T> messageEn(String messageEn) {
            this.messageEn = messageEn;
            return this;
        }
        
        public ResultBuilder<T> data(T data) {
            this.data = data;
            return this;
        }
        
        public ResultBuilder<T> timestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
            return this;
        }
        
        public ResultBuilder<T> requestId(String requestId) {
            this.requestId = requestId;
            return this;
        }
        
        public ResultBuilder<T> generateRequestId() {
            this.requestId = UUID.randomUUID().toString().replace("-", "");
            return this;
        }
        
        public Result<T> build() {
            Result<T> result = new Result<>();
            result.setCode(this.code);
            result.setMessage(this.message);
            result.setData(this.data);
            result.setTimestamp(System.currentTimeMillis());
            result.setRequestId(this.requestId);
            return result;
        }
    }
    
    // ==================== 工具方法 ====================
    
    /**
     * 判断响应是否成功
     * 
     * @param result 响应结果
     * @return 是否成功
     */
    public static boolean isSuccess(Result<?> result) {
        return result != null && result.isSuccess();
    }
    
    /**
     * 判断响应是否失败
     * 
     * @param result 响应结果
     * @return 是否失败
     */
    public static boolean isError(Result<?> result) {
        return result != null && result.isError();
    }
    
    /**
     * 获取响应数据，如果失败则返回null
     * 
     * @param result 响应结果
     * @return 响应数据或null
     */
    public static <T> T getData(Result<T> result) {
        return isSuccess(result) ? result.getData() : null;
    }
    
    /**
     * 获取响应数据，如果失败则返回默认值
     * 
     * @param result 响应结果
     * @param defaultValue 默认值
     * @return 响应数据或默认值
     */
    public static <T> T getDataOrDefault(Result<T> result, T defaultValue) {
        return isSuccess(result) ? result.getData() : defaultValue;
    }
    
    /**
     * 复制响应结果（不包含数据）
     * 
     * @param source 源响应
     * @return 新的响应结果
     */
    public static <T> Result<T> copyWithoutData(Result<?> source) {
        Result<T> result = new Result<>();
        result.setCode(source.getCode());
        result.setMessage(source.getMessage());
        result.setTimestamp(source.getTimestamp());
        result.setRequestId(source.getRequestId());
        return result;
    }
    
    /**
     * 转换响应数据类型
     * 
     * @param source 源响应
     * @param newData 新数据
     * @return 新的响应结果
     */
    public static <T, R> Result<R> transform(Result<T> source, R newData) {
        Result<R> result = new Result<>();
        result.setCode(source.getCode());
        result.setMessage(source.getMessage());
        result.setData(newData);
        result.setTimestamp(source.getTimestamp());
        result.setRequestId(source.getRequestId());
        return result;
    }
}