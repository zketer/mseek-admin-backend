package com.lynn.museum.common.web.exception;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.common.result.ResultCode;
import com.lynn.museum.common.exception.BizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.nio.file.AccessDeniedException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.stream.Collectors;

/**
 * 全局异常处理器
 * 统一处理系统中的各种异常，返回标准的响应格式
 * @author lynn
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // ==================== 业务异常处理 ====================
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BizException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBizException(BizException ex) {
        log.error("业务异常: [{}] {}", ex.getCode(), ex.getMessage());
        return Result.error(ex.getCode(), ex.getMessage(), ex.getMessageEn(), ex.getData());
    }
    

    
    // ==================== 参数校验异常处理 ====================
    
    /**
     * 处理方法参数校验异常（@Valid）
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.warn("参数校验失败: {}", ex.getMessage());
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return Result.error(ResultCode.PARAM_ERROR.getCode(), 
                "参数校验失败: " + errorMessage, 
                "Parameter validation failed: " + errorMessage);
    }
    
    /**
     * 处理表单绑定异常
     */
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleBindException(BindException ex) {
        log.warn("表单绑定异常: {}", ex.getMessage());
        String errorMessage = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return Result.error(ResultCode.PARAM_ERROR.getCode(), 
                "表单绑定失败: " + errorMessage, 
                "Form binding failed: " + errorMessage);
    }
    
    /**
     * 处理缺少请求参数异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
        log.warn("缺少请求参数: {}", ex.getMessage());
        String message = String.format("缺少必要参数: %s", ex.getParameterName());
        String messageEn = String.format("Missing required parameter: %s", ex.getParameterName());
        return Result.error(ResultCode.PARAM_MISSING.getCode(), message, messageEn);
    }
    
    /**
     * 处理方法参数类型不匹配异常
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException ex) {
        log.warn("参数类型不匹配: {}", ex.getMessage());
        String message = String.format("参数 %s 类型不正确，期望类型: %s", 
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        String messageEn = String.format("Parameter %s type mismatch, expected type: %s", 
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown");
        return Result.error(ResultCode.PARAM_INVALID.getCode(), message, messageEn);
    }
    
    // ==================== HTTP相关异常处理 ====================
    
    /**
     * 处理请求方法不支持异常
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public Result<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        log.warn("请求方法不支持: {}", ex.getMessage());
        String message = String.format("请求方法 %s 不支持，支持的方法: %s", 
                ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
        String messageEn = String.format("Request method %s not supported, supported methods: %s", 
                ex.getMethod(), String.join(", ", ex.getSupportedMethods()));
        return Result.error(ResultCode.METHOD_NOT_ALLOWED.getCode(), message, messageEn);
    }
    
    /**
     * 处理媒体类型不支持异常
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    @ResponseStatus(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
    public Result<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        log.warn("媒体类型不支持: {}", ex.getMessage());
        String message = String.format("媒体类型 %s 不支持", ex.getContentType());
        String messageEn = String.format("Media type %s not supported", ex.getContentType());
        return Result.error(415, message, messageEn);
    }
    
    /**
     * 处理HTTP消息不可读异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex) {
        log.warn("HTTP消息不可读: {}", ex.getMessage());
        return Result.error(ResultCode.BAD_REQUEST.getCode(), 
                "请求体格式错误", 
                "Request body format error");
    }
    
    /**
     * 处理404异常
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Result<Object> handleNoHandlerFound(NoHandlerFoundException ex) {
        log.warn("请求路径不存在: {}", ex.getRequestURL());
        String message = String.format("请求路径 %s 不存在", ex.getRequestURL());
        String messageEn = String.format("Request path %s not found", ex.getRequestURL());
        return Result.error(ResultCode.NOT_FOUND.getCode(), message, messageEn);
    }
    
    // ==================== 文件上传异常处理 ====================
    
    /**
     * 处理文件上传大小超限异常
     */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        log.warn("文件上传大小超限: {}", ex.getMessage());
        return Result.error(ResultCode.FILE_SIZE_EXCEEDED);
    }
    
    // ==================== 安全相关异常处理 ====================
    
    /**
     * 处理访问拒绝异常
     */
    @ExceptionHandler(AccessDeniedException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Result<Object> handleAccessDenied(AccessDeniedException ex) {
        log.warn("访问被拒绝: {}", ex.getMessage());
        return Result.error(ResultCode.FORBIDDEN);
    }
    
    // ==================== 数据库相关异常处理 ====================
    
    /**
     * 处理SQL完整性约束违反异常
     */
    @ExceptionHandler(SQLIntegrityConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleSqlIntegrityConstraintViolation(SQLIntegrityConstraintViolationException ex) {
        log.error("SQL完整性约束违反异常: {}", ex.getMessage());
        
        if (ex.getMessage().contains("Duplicate entry")) {
            String duplicateValue = extractDuplicateValue(ex.getMessage());
            String message = String.format("数据重复: %s", duplicateValue);
            String messageEn = String.format("Duplicate data: %s", duplicateValue);
            return Result.error(ResultCode.DB_DUPLICATE_KEY.getCode(), message, messageEn);
        }
        
        if (ex.getMessage().contains("cannot be null")) {
            return Result.error(ResultCode.DB_CONSTRAINT_VIOLATION.getCode(), 
                    "必填字段不能为空", 
                    "Required field cannot be null");
        }
        
        return Result.error(ResultCode.DB_CONSTRAINT_VIOLATION);
    }
    
    // ==================== 运行时异常处理 ====================
    
    /**
     * 处理不支持的操作异常（如工具类实例化）
     */
    @ExceptionHandler(UnsupportedOperationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Result<Object> handleUnsupportedOperation(UnsupportedOperationException ex) {
        log.warn("不支持的操作: {}", ex.getMessage());
        return Result.error(ResultCode.OPERATION_NOT_SUPPORTED);
    }
    
    // ==================== 通用异常处理 ====================
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleRuntimeException(RuntimeException ex) {
        log.error("运行时异常: {}", ex.getMessage(), ex);
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), 
                "系统运行异常", 
                "System runtime error");
    }
    
    /**
     * 处理其他所有未处理的异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Result<Object> handleGeneralException(Exception ex) {
        log.error("系统异常: {}", ex.getMessage(), ex);
        return Result.error(ResultCode.SYSTEM_ERROR.getCode(), 
                "系统异常，请联系管理员", 
                "System error, please contact administrator");
    }
    
    // ==================== 私有工具方法 ====================
    
    /**
     * 从SQL异常消息中提取重复的值
     */
    private String extractDuplicateValue(String message) {
        try {
            // 匹配 "Duplicate entry 'value' for key 'key_name'"
            if (message.contains("Duplicate entry")) {
                int start = message.indexOf("'") + 1;
                int end = message.indexOf("'", start);
                if (start > 0 && end > start) {
                    return message.substring(start, end);
                }
            }
            return "unknown";
        } catch (Exception e) {
            return "unknown";
        }
    }
}