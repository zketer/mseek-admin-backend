package com.lynn.museum.common.exception;

import com.lynn.museum.common.result.ResultCode;
import lombok.*;

/**
 * 业务异常类
 * 用于处理业务逻辑中的异常情况
 * @author lynn
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class BizException extends RuntimeException {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 错误码
     */
    private Integer code;
    
    /**
     * 错误消息（中文）
     */
    private String message;
    
    /**
     * 错误消息（英文）
     */
    private String messageEn;
    
    /**
     * 异常数据（可选）
     */
    private Object data;
    
    /**
     * 构造函数：仅消息（默认500错误码）
     */
    public BizException(String message) {
        super(message);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
        this.messageEn = message;
    }
    
    /**
     * 构造函数：错误码 + 消息
     */
    public BizException(Integer code, String message) {
        super(message);
        this.code = code;
        this.message = message;
        this.messageEn = message;
    }
    
    /**
     * 构造函数：错误码 + 中英文消息
     */
    public BizException(Integer code, String message, String messageEn) {
        super(message);
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
    }
    
    /**
     * 构造函数：错误码 + 中英文消息 + 数据
     */
    public BizException(Integer code, String message, String messageEn, Object data) {
        super(message);
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
        this.data = data;
    }
    
    /**
     * 构造函数：ResultCode枚举
     */
    public BizException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.messageEn = resultCode.getMessageEn();
    }
    
    /**
     * 构造函数：ResultCode枚举 + 自定义消息
     */
    public BizException(ResultCode resultCode, String customMessage) {
        super(customMessage);
        this.code = resultCode.getCode();
        this.message = customMessage;
        this.messageEn = customMessage;
    }
    
    /**
     * 构造函数：ResultCode枚举 + 自定义中英文消息
     */
    public BizException(ResultCode resultCode, String customMessage, String customMessageEn) {
        super(customMessage);
        this.code = resultCode.getCode();
        this.message = customMessage;
        this.messageEn = customMessageEn;
    }
    
    /**
     * 构造函数：ResultCode枚举 + 数据
     */
    public BizException(ResultCode resultCode, Object data) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.messageEn = resultCode.getMessageEn();
        this.data = data;
    }
    
    /**
     * 构造函数：ResultCode枚举 + 自定义消息 + 数据
     */
    public BizException(ResultCode resultCode, String customMessage, Object data) {
        super(customMessage);
        this.code = resultCode.getCode();
        this.message = customMessage;
        this.messageEn = customMessage;
        this.data = data;
    }
    
    /**
     * 构造函数：包含原始异常
     */
    public BizException(String message, Throwable cause) {
        super(message, cause);
        this.code = ResultCode.INTERNAL_SERVER_ERROR.getCode();
        this.message = message;
        this.messageEn = message;
    }
    
    /**
     * 构造函数：ResultCode枚举 + 原始异常
     */
    public BizException(ResultCode resultCode, Throwable cause) {
        super(resultCode.getMessage(), cause);
        this.code = resultCode.getCode();
        this.message = resultCode.getMessage();
        this.messageEn = resultCode.getMessageEn();
    }
    
    /**
     * 构造函数：完整参数 + 原始异常
     */
    public BizException(Integer code, String message, String messageEn, Object data, Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
        this.data = data;
    }
    
    // ==================== 静态工厂方法 ====================
    
    /**
     * 创建业务异常（使用ResultCode）
     */
    public static BizException of(ResultCode resultCode) {
        return new BizException(resultCode);
    }
    
    /**
     * 创建业务异常（使用ResultCode + 自定义消息）
     */
    public static BizException of(ResultCode resultCode, String customMessage) {
        return new BizException(resultCode, customMessage);
    }
    
    /**
     * 创建业务异常（使用ResultCode + 数据）
     */
    public static BizException of(ResultCode resultCode, Object data) {
        return new BizException(resultCode, data);
    }
    
    /**
     * 创建业务异常（使用ResultCode + 自定义消息 + 数据）
     */
    public static BizException of(ResultCode resultCode, String customMessage, Object data) {
        return new BizException(resultCode, customMessage, data);
    }
    
    /**
     * 创建参数错误异常
     */
    public static BizException paramError() {
        return new BizException(ResultCode.PARAM_ERROR);
    }
    
    /**
     * 创建参数错误异常（自定义消息）
     */
    public static BizException paramError(String message) {
        return new BizException(ResultCode.PARAM_ERROR, message);
    }
    
    /**
     * 创建数据不存在异常
     */
    public static BizException dataNotFound() {
        return new BizException(ResultCode.DATA_NOT_FOUND);
    }
    
    /**
     * 创建数据不存在异常（自定义消息）
     */
    public static BizException dataNotFound(String message) {
        return new BizException(ResultCode.DATA_NOT_FOUND, message);
    }
    
    /**
     * 创建权限不足异常
     */
    public static BizException permissionDenied() {
        return new BizException(ResultCode.PERMISSION_DENIED);
    }
    
    /**
     * 创建权限不足异常（自定义消息）
     */
    public static BizException permissionDenied(String message) {
        return new BizException(ResultCode.PERMISSION_DENIED, message);
    }
    
    /**
     * 创建未授权异常
     */
    public static BizException unauthorized() {
        return new BizException(ResultCode.UNAUTHORIZED);
    }
    
    /**
     * 创建未授权异常（自定义消息）
     */
    public static BizException unauthorized(String message) {
        return new BizException(ResultCode.UNAUTHORIZED, message);
    }
    
    // ==================== 实用方法 ====================
    
    /**
     * 获取完整的错误信息（包含错误码）
     */
    public String getFullMessage() {
        return String.format("[%d] %s", code, message);
    }
    
    /**
     * 获取完整的英文错误信息（包含错误码）
     */
    public String getFullMessageEn() {
        return String.format("[%d] %s", code, messageEn != null ? messageEn : message);
    }
    
    /**
     * 判断是否为指定错误码
     */
    public boolean isCode(ResultCode resultCode) {
        return resultCode.getCode().equals(this.code);
    }
    
    /**
     * 判断是否为指定错误码
     */
    public boolean isCode(Integer code) {
        return code.equals(this.code);
    }
}