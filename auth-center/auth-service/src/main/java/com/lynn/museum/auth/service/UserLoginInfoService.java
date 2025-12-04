package com.lynn.museum.auth.service;

import com.lynn.museum.auth.dto.UserLoginInfoResponse;

/**
 * 用户登录信息服务接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface UserLoginInfoService {
    
    /**
     * 获取用户登录信息
     * 
     * @param userId 用户ID
     * @return 用户登录信息
     */
    UserLoginInfoResponse getUserLoginInfo(Long userId);
    
    /**
     * 记录用户登录信息
     * 
     * @param userId 用户ID
     * @param username 用户名
     * @param loginIp 登录IP
     * @param userAgent 用户代理
     * @param loginResult 登录结果 1-成功 0-失败
     * @param failureReason 失败原因（登录成功时为null）
     */
    void recordLoginInfo(Long userId, String username, String loginIp, String userAgent, Integer loginResult, String failureReason);
}
