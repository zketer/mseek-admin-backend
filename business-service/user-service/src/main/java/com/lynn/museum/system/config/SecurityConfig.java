package com.lynn.museum.system.config;

import com.lynn.museum.common.security.BaseSecurityConfig;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * 用户服务安全配置类
 * 
 * 继承通用的BaseSecurityConfig，实现统一的安全策略：
 * 1. 认证由API网关统一处理
 * 2. 服务内部允许所有请求通过Spring Security  
 * 3. 支持方法级安全注解（如@PreAuthorize）
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig extends BaseSecurityConfig {

    /**
     * 配置用户服务的特定授权规则
     * 
     * 由于认证通过API网关处理，这里允许所有请求通过Spring Security
     * 具体的权限控制可通过方法级注解实现
     */
    @Override
    protected void configureAuthorization(
        org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz
    ) {
        // 允许所有请求通过（认证由API网关处理）
        authz.anyRequest().permitAll();
    }
}