package com.lynn.museum.api.auth.client;

import com.lynn.museum.api.auth.dto.UserLoginInfo;
import com.lynn.museum.common.result.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 认证服务API客户端
 * 
 * @author lynn
 * @since 2024-01-01
 */
@FeignClient(name = "auth-service", path = "/api/v1/auth")
public interface AuthApiClient {
    
    /**
     * 获取用户登录信息
     * 
     * @param userId 用户ID
     * @return 用户登录信息
     */
    @GetMapping("/users/{userId}/login-info")
    Result<UserLoginInfo> getUserLoginInfo(@PathVariable("userId") Long userId);
}