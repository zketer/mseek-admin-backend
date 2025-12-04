package com.lynn.museum.common.auth;

import org.springframework.http.HttpHeaders;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.HashMap;

/**
 * 内部服务认证工具类
 * 
 * 提供内部服务间调用的认证绕过机制：
 * 1. 设置内部调用Header
 * 2. 生成服务Token
 * 3. 验证内部调用标识
 * 
 * @author lynn
 * @since 2024-01-01
 */
public class InternalServiceAuthUtil {
    
    private static final String INTERNAL_CALL_HEADER = "X-Internal-Call";
    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";
    private static final String CLIENT_IP_HEADER = "X-Client-IP";
    
    /**
     * 创建内部服务调用Headers
     * 
     * @param serviceId 服务ID
     * @return Headers Map
     */
    public static Map<String, String> createInternalCallHeaders(String serviceId) {
        Map<String, String> headers = new HashMap<>();
        headers.put(INTERNAL_CALL_HEADER, "true");
        headers.put(SERVICE_ID_HEADER, serviceId);
        return headers;
    }
    
    /**
     * 创建带服务Token的内部调用Headers
     * 
     * @param serviceId 服务ID
     * @param serviceToken 服务Token
     * @return Headers Map
     */
    public static Map<String, String> createInternalCallHeaders(String serviceId, String serviceToken) {
        Map<String, String> headers = createInternalCallHeaders(serviceId);
        if (StringUtils.hasText(serviceToken)) {
            headers.put(SERVICE_TOKEN_HEADER, serviceToken);
        }
        return headers;
    }
    
    /**
     * 为HttpHeaders设置内部调用标识
     * 
     * @param httpHeaders Spring HttpHeaders对象
     * @param serviceId 服务ID
     */
    public static void setInternalCallHeaders(HttpHeaders httpHeaders, String serviceId) {
        httpHeaders.set(INTERNAL_CALL_HEADER, "true");
        httpHeaders.set(SERVICE_ID_HEADER, serviceId);
    }
    
    /**
     * 为HttpHeaders设置内部调用标识（带Token）
     * 
     * @param httpHeaders Spring HttpHeaders对象
     * @param serviceId 服务ID
     * @param serviceToken 服务Token
     */
    public static void setInternalCallHeaders(HttpHeaders httpHeaders, String serviceId, String serviceToken) {
        setInternalCallHeaders(httpHeaders, serviceId);
        if (StringUtils.hasText(serviceToken)) {
            httpHeaders.set(SERVICE_TOKEN_HEADER, serviceToken);
        }
    }
    
    /**
     * 生成默认的服务Token
     * 
     * @param serviceId 服务ID
     * @return 服务Token
     */
    public static String generateServiceToken(String serviceId) {
        // 生成服务Token（生产环境建议使用更安全的算法）
        return "museum-service-" + serviceId + "-" + System.currentTimeMillis();
    }
    
    /**
     * 验证是否为内部服务调用
     * 
     * @param headers Headers Map
     * @return true 如果是内部服务调用
     */
    public static boolean isInternalCall(Map<String, String> headers) {
        if (headers == null) {
            return false;
        }
        
        String internalCall = headers.get(INTERNAL_CALL_HEADER);
        return "true".equalsIgnoreCase(internalCall);
    }
    
    /**
     * 验证是否为内部服务调用
     * 
     * @param httpHeaders Spring HttpHeaders对象
     * @return true 如果是内部服务调用
     */
    public static boolean isInternalCall(HttpHeaders httpHeaders) {
        if (httpHeaders == null) {
            return false;
        }
        
        String internalCall = httpHeaders.getFirst(INTERNAL_CALL_HEADER);
        return "true".equalsIgnoreCase(internalCall);
    }
    
    /**
     * 从Headers中获取服务ID
     * 
     * @param headers Headers Map
     * @return 服务ID
     */
    public static String getServiceId(Map<String, String> headers) {
        return headers != null ? headers.get(SERVICE_ID_HEADER) : null;
    }
    
    /**
     * 从Headers中获取服务ID
     * 
     * @param httpHeaders Spring HttpHeaders对象
     * @return 服务ID
     */
    public static String getServiceId(HttpHeaders httpHeaders) {
        return httpHeaders != null ? httpHeaders.getFirst(SERVICE_ID_HEADER) : null;
    }
    
    /**
     * 从Headers中获取服务Token
     * 
     * @param headers Headers Map
     * @return 服务Token
     */
    public static String getServiceToken(Map<String, String> headers) {
        return headers != null ? headers.get(SERVICE_TOKEN_HEADER) : null;
    }
    
    /**
     * 从Headers中获取服务Token
     * 
     * @param httpHeaders Spring HttpHeaders对象
     * @return 服务Token
     */
    public static String getServiceToken(HttpHeaders httpHeaders) {
        return httpHeaders != null ? httpHeaders.getFirst(SERVICE_TOKEN_HEADER) : null;
    }
    
    /**
     * 验证服务Token是否有效
     * 
     * @param serviceToken 服务Token
     * @param serviceId 服务ID
     * @return true 如果Token有效
     */
    public static boolean isValidServiceToken(String serviceToken, String serviceId) {
        if (!StringUtils.hasText(serviceToken) || !StringUtils.hasText(serviceId)) {
            return false;
        }
        
        // Token验证逻辑（当前为简单实现，生产环境可增强安全性）
        return serviceToken.startsWith("museum-service-" + serviceId);
    }
    
    /**
     * 创建Feign调用的内部认证Interceptor使用的Headers
     * 
     * @param serviceId 当前服务ID
     * @return 用于Feign Interceptor的Headers
     */
    public static Map<String, String> createFeignInternalHeaders(String serviceId) {
        return createInternalCallHeaders(serviceId, generateServiceToken(serviceId));
    }
}
