package com.lynn.museum.system.utils;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 认证上下文工具类
 * 用于获取Gateway传递的用户信息
 * 
 * @author lynn
 * @since 2024-01-01
 */
public class AuthContextUtils {
    
    private static final String USER_ID_HEADER = "X-User-Id";
    private static final String USERNAME_HEADER = "X-Username";
    
    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        String userIdStr = request.getHeader(USER_ID_HEADER);
        if (userIdStr == null || userIdStr.isEmpty()) {
            return null;
        }
        
        try {
            return Long.parseLong(userIdStr);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        return request.getHeader(USERNAME_HEADER);
    }
    
    /**
     * 检查是否已认证（是否有用户信息）
     */
    public static boolean isAuthenticated() {
        return getCurrentUserId() != null && getCurrentUsername() != null;
    }
    
    /**
     * 获取当前HTTP请求
     */
    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        return attributes != null ? attributes.getRequest() : null;
    }
}
