package com.lynn.museum.common.feign;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.common.result.ResultCode;
import feign.Response;
import feign.Util;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 全局Feign错误解码器
 * 
 * 功能：
 * 1. 拦截所有Feign调用的HTTP错误响应（4xx、5xx）
 * 2. 尝试从响应体中解析业务错误信息（Result结构）
 * 3. 将HTTP错误转换为业务异常（BizException），保留原始错误信息
 * 4. 如果无法解析响应体，则根据HTTP状态码返回通用异常
 * 
 * 优势：
 * - 全局生效，所有@FeignClient接口都自动处理错误
 * - 避免在每个Service方法中手动catch FeignException
 * - 统一错误处理逻辑，降低维护成本
 * - 保留原始错误信息，方便日志记录和问题排查
 * 
 * 使用场景示例：
 * - user-service返回: {"code":300000,"message":"用户不存在"}
 * - ErrorDecoder自动解析并抛出: BizException(USER_NOT_FOUND, "用户不存在")
 * - 调用方无需手动处理FeignException，直接catch BizException即可
 * 
 * 条件加载：
 * - 只有当 ErrorDecoder 类存在时才加载（避免非Feign服务启动失败）
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Configuration
@ConditionalOnClass(ErrorDecoder.class)
public class GlobalFeignErrorDecoder implements ErrorDecoder {
    
    private final ErrorDecoder defaultErrorDecoder = new Default();
    
    @Override
    public Exception decode(String methodKey, Response response) {
        log.debug("[Feign] 捕获HTTP错误响应: method={}, status={}", methodKey, response.status());
        
        try {
            // 1. 尝试读取响应体
            String responseBody = null;
            if (response.body() != null) {
                responseBody = Util.toString(response.body().asReader(StandardCharsets.UTF_8));
                log.debug("[Feign] 错误响应体: {}", responseBody);
            }
            
            // 2. 尝试解析为标准Result结构
            if (StrUtil.isNotBlank(responseBody)) {
                try {
                    Result<?> errorResult = JSONUtil.toBean(responseBody, Result.class);
                    
                    // 3. 如果成功解析Result，并且包含错误信息
                    if (errorResult != null && errorResult.getCode() != null) {
                        String errorMessage = StrUtil.isNotBlank(errorResult.getMessage()) 
                            ? errorResult.getMessage() 
                            : "远程服务调用失败";
                        
                        log.warn("[Feign] 解析到业务错误: method={}, code={}, message={}", 
                            methodKey, errorResult.getCode(), errorMessage);
                        
                        // 4. 转换为BizException，保留原始错误码和消息
                        return new BizException(errorResult.getCode(), errorMessage);
                    }
                } catch (Exception parseException) {
                    log.debug("[Feign] 响应体不是标准Result格式，尝试其他解析方式: {}", parseException.getMessage());
                }
            }
            
            // 5. 如果无法解析为Result，根据HTTP状态码返回通用异常
            return createExceptionByHttpStatus(methodKey, response.status(), responseBody);
            
        } catch (IOException e) {
            log.error("[Feign] 读取错误响应体失败: method={}, error={}", methodKey, e.getMessage(), e);
            return createExceptionByHttpStatus(methodKey, response.status(), null);
        } catch (Exception e) {
            log.error("[Feign] 错误解码异常: method={}, error={}", methodKey, e.getMessage(), e);
            // 使用默认解码器作为兜底
            return defaultErrorDecoder.decode(methodKey, response);
        }
    }
    
    /**
     * 根据HTTP状态码创建对应的业务异常
     */
    private Exception createExceptionByHttpStatus(String methodKey, int status, String responseBody) {
        log.warn("[Feign] 根据HTTP状态码创建异常: method={}, status={}", methodKey, status);
        
        // 客户端错误 (4xx)
        if (status >= 400 && status < 500) {
            switch (status) {
                case 400:
                    return new BizException(ResultCode.PARAM_ERROR.getCode(), 
                        StrUtil.isNotBlank(responseBody) ? responseBody : "请求参数错误");
                case 401:
                    return new BizException(ResultCode.UNAUTHORIZED);
                case 403:
                    return new BizException(ResultCode.FORBIDDEN);
                case 404:
                    return new BizException(ResultCode.NOT_FOUND.getCode(), "请求的资源不存在");
                case 409:
                    return new BizException(ResultCode.PARAM_ERROR.getCode(), "请求冲突");
                default:
                    return new BizException(ResultCode.PARAM_ERROR.getCode(), 
                        String.format("客户端请求错误: HTTP %d", status));
            }
        }
        
        // 服务端错误 (5xx)
        if (status >= 500) {
            switch (status) {
                case 500:
                    return new BizException(ResultCode.SYSTEM_ERROR.getCode(), "远程服务内部错误");
                case 502:
                    return new BizException(ResultCode.SYSTEM_ERROR.getCode(), "网关错误");
                case 503:
                    return new BizException(ResultCode.SYSTEM_ERROR.getCode(), "服务暂时不可用");
                case 504:
                    return new BizException(ResultCode.SYSTEM_ERROR.getCode(), "网关超时");
                default:
                    return new BizException(ResultCode.SYSTEM_ERROR.getCode(), 
                        String.format("远程服务错误: HTTP %d", status));
            }
        }
        
        // 其他情况，使用默认解码器
        log.warn("[Feign] 未知HTTP状态码: {}, 使用默认解码器", status);
        return new BizException(ResultCode.SYSTEM_ERROR.getCode(), 
            String.format("远程服务调用失败: HTTP %d", status));
    }
}

