package com.lynn.museum.common.web.utils;

import cn.hutool.core.util.StrUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * 请求工具类
 * 提供统一的请求信息获取方法
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
public class RequestUtils {

    private static final String UNKNOWN = "unknown";
    
    /**
     * 获取客户端真实IP地址
     * 
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        String ip = null;
        
        // 1. 检查X-Forwarded-For头（通过代理时使用）
        ip = request.getHeader("X-Forwarded-For");
        if (isValidIp(ip)) {
            // 可能包含多个IP，取第一个
            return ip.split(",")[0].trim();
        }
        
        // 2. 检查X-Real-IP头（Nginx代理常用）
        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 3. 检查HTTP_CLIENT_IP头
        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 4. 检查HTTP_X_FORWARDED_FOR头
        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 5. 检查WL-Proxy-Client-IP头（WebLogic代理）
        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 6. 检查HTTP_X_FORWARDED头
        ip = request.getHeader("HTTP_X_FORWARDED");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 7. 检查HTTP_X_CLUSTER_CLIENT_IP头
        ip = request.getHeader("HTTP_X_CLUSTER_CLIENT_IP");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 8. 检查HTTP_FORWARDED_FOR头
        ip = request.getHeader("HTTP_FORWARDED_FOR");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 9. 检查HTTP_FORWARDED头
        ip = request.getHeader("HTTP_FORWARDED");
        if (isValidIp(ip)) {
            return ip;
        }
        
        // 10. 最后使用getRemoteAddr()
        ip = request.getRemoteAddr();
        if (isValidIp(ip)) {
            return ip;
        }
        
        return UNKNOWN;
    }
    
    /**
     * 检查IP是否有效
     * 
     * @param ip IP地址
     * @return 是否有效
     */
    private static boolean isValidIp(String ip) {
        return StrUtil.isNotBlank(ip) && 
               !UNKNOWN.equalsIgnoreCase(ip) &&
               !"0:0:0:0:0:0:0:1".equals(ip);
    }
    
    /**
     * 获取用户代理信息
     * 
     * @param request HTTP请求对象
     * @return 用户代理字符串
     */
    public static String getUserAgent(HttpServletRequest request) {
        if (request == null) {
            return UNKNOWN;
        }
        
        String userAgent = request.getHeader("User-Agent");
        return StrUtil.isNotBlank(userAgent) ? userAgent : UNKNOWN;
    }
    
    /**
     * 获取请求的完整URL
     * 
     * @param request HTTP请求对象
     * @return 完整URL
     */
    public static String getFullRequestUrl(HttpServletRequest request) {
        if (request == null) {
            return "";
        }
        
        StringBuilder url = new StringBuilder();
        url.append(request.getScheme())
           .append("://")
           .append(request.getServerName());
           
        if (request.getServerPort() != 80 && request.getServerPort() != 443) {
            url.append(":").append(request.getServerPort());
        }
        
        url.append(request.getRequestURI());
        
        if (StrUtil.isNotBlank(request.getQueryString())) {
            url.append("?").append(request.getQueryString());
        }
        
        return url.toString();
    }
}
