package com.lynn.museum.common.web.utils;

import cn.hutool.core.util.StrUtil;
import com.lynn.museum.common.constants.ValidationConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户上下文工具类
 * 用于获取当前登录用户信息
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
public class UserContextUtils {

    /**
     * Token请求头名称
     */
    private static final String TOKEN_HEADER = "Authorization";

    /**
     * Token前缀
     */
    private static final String TOKEN_PREFIX = "Bearer ";

    /**
     * 用户ID请求头名称
     */
    private static final String USER_ID_HEADER = "X-User-Id";

    /**
     * 用户名请求头名称
     */
    private static final String USERNAME_HEADER = "X-Username";

    /**
     * 获取当前请求
     */
    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }

    /**
     * 获取Token
     */
    public static String getToken() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        String token = request.getHeader(TOKEN_HEADER);
        if (StrUtil.isNotBlank(token) && token.startsWith(TOKEN_PREFIX)) {
            return token.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    /**
     * 获取当前用户ID
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        // 优先从请求头获取
        String userIdStr = request.getHeader(USER_ID_HEADER);
        if (StrUtil.isNotBlank(userIdStr)) {
            try {
                return Long.parseLong(userIdStr);
            } catch (NumberFormatException e) {
                log.warn("用户ID格式错误: {}", userIdStr);
            }
        }
        
        // Gateway统一处理认证，不再需要解析Token
        
        return null;
    }

    /**
     * 获取当前用户名
     */
    public static String getCurrentUsername() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        // 优先从请求头获取
        String username = request.getHeader(USERNAME_HEADER);
        if (StrUtil.isNotBlank(username)) {
            return username;
        }
        
        // Gateway统一处理认证，不再需要解析Token
        
        return null;
    }

    /**
     * 获取客户端IP
     */
    public static String getClientIp() {
        HttpServletRequest request = getCurrentRequest();
        if (request == null) {
            return null;
        }
        
        String ip = request.getHeader("X-Forwarded-For");
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StrUtil.isBlank(ip) || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况，取第一个
        if (StrUtil.isNotBlank(ip) && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip;
    }

    /**
     * 获取User-Agent
     */
    public static String getUserAgent() {
        HttpServletRequest request = getCurrentRequest();
        return request != null ? request.getHeader("User-Agent") : null;
    }

    /**
     * 检查是否已登录
     */
    public static boolean isLoggedIn() {
        return getCurrentUserId() != null;
    }

    /**
     * 设置用户信息到请求头（用于网关转发）
     */
    public static void setUserInfo(HttpServletRequest request, Long userId, String username) {
        if (request != null) {
            request.setAttribute(USER_ID_HEADER, userId);
            request.setAttribute(USERNAME_HEADER, username);
        }
    }

}