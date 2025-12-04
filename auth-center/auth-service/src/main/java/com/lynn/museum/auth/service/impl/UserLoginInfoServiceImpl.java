package com.lynn.museum.auth.service.impl;

import java.util.Date;

import com.lynn.museum.auth.dto.UserLoginInfoResponse;
import com.lynn.museum.auth.mapper.AuthLoginLogMapper;
import com.lynn.museum.auth.model.entity.AuthLoginLog;
import com.lynn.museum.auth.service.UserLoginInfoService;
import com.lynn.museum.common.web.utils.RequestUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;

/**
 * 用户登录信息服务实现类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserLoginInfoServiceImpl implements UserLoginInfoService {

    private final AuthLoginLogMapper authLoginLogMapper;

    @Override
    public UserLoginInfoResponse getUserLoginInfo(Long userId) {
        if (userId == null) {
            return UserLoginInfoResponse.builder()
                    .loginCount(0)
                    .build();
        }

        try {
            // 获取登录次数
            Integer loginCount = authLoginLogMapper.countSuccessLoginsByUserId(userId);
            
            UserLoginInfoResponse.UserLoginInfoResponseBuilder builder = UserLoginInfoResponse.builder()
                    .loginCount(loginCount != null ? loginCount : 0);
            
            // 从登录日志表获取最后登录信息
            AuthLoginLog lastLogin = authLoginLogMapper.getLastSuccessLoginByUserId(userId);
            if (lastLogin != null) {
                builder.lastLoginTime(lastLogin.getCreateAt())
                       .lastLoginIp(lastLogin.getLoginIp())
                       .loginLocation(lastLogin.getLoginLocation())
                       .deviceType(lastLogin.getDeviceType())
                       .userAgent(lastLogin.getUserAgent());
            }
            
            return builder.build();
            
        } catch (Exception e) {
            log.error("获取用户{}登录信息失败", userId, e);
            return UserLoginInfoResponse.builder()
                    .loginCount(0)
                    .build();
        }
    }

    @Override
    public void recordLoginInfo(Long userId, String username, String loginIp, String userAgent, Integer loginResult, String failureReason) {
        try {
            // 记录登录日志
            AuthLoginLog loginLog = new AuthLoginLog();
            loginLog.setUserId(userId);
            loginLog.setUsername(username);
            // 默认密码登录
            loginLog.setLoginType(1);
            loginLog.setLoginResult(loginResult);
            loginLog.setFailureReason(failureReason);
            loginLog.setLoginIp(loginIp);
            loginLog.setUserAgent(userAgent);
            loginLog.setDeviceType(parseDeviceType(userAgent));
            // createAt由MyBatis Plus的@TableField(fill = FieldFill.INSERT)自动填充
            
            authLoginLogMapper.insert(loginLog);
            
            // 记录登录结果
            if (loginResult != null && loginResult == 1) {
                log.info("用户{}登录成功，IP: {}", username, loginIp);
            } else {
                log.warn("用户{}登录失败，原因: {}", username, failureReason);
            }
            
        } catch (Exception e) {
            log.error("记录用户{}登录信息失败", username, e);
        }
    }
    
    /**
     * 解析设备类型
     */
    private String parseDeviceType(String userAgent) {
        if (userAgent == null) {
            return "unknown";
        }
        
        String ua = userAgent.toLowerCase();
        if (ua.contains("mobile") || ua.contains("android") || ua.contains("iphone")) {
            return "mobile";
        } else if (ua.contains("tablet") || ua.contains("ipad")) {
            return "tablet";
        } else {
            return "desktop";
        }
    }
}
