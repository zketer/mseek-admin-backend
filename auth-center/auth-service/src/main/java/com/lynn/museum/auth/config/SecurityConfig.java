package com.lynn.museum.auth.config;

import com.lynn.museum.common.security.BaseSecurityConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

/**
 * 认证服务安全配置类
 * 
 * 继承通用的BaseSecurityConfig，实现统一的安全策略：
 * 1. 自动支持内部服务调用认证（基于Header检查，无硬编码URL）
 * 2. 认证相关的公开接口配置
 * 3. 支持方法级安全注解（如@PreAuthorize）
 * 
 * 优化说明：
 * - 使用通用的内部服务认证过滤器，避免硬编码URL路径
 * - 所有内部服务接口都通过Header自动识别和认证
 * - 符合微服务架构的通用性和可扩展性原则
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig extends BaseSecurityConfig {

    /**
     * 配置认证服务的特定授权规则
     * 
     * 只需配置业务相关的公开接口，内部服务调用通过Filter自动处理
     */
    @Override
    protected void configureAuthorization(
        org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<org.springframework.security.config.annotation.web.builders.HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz
    ) {
        authz
            // 认证相关公开接口（相对于context-path: /api/v1/auth）
            .requestMatchers("/captcha", "/login", "/register", "/send-code", "/reset-password", "/refresh", "/validate").permitAll()
            // JWKS端点（供Gateway获取公钥）
            .requestMatchers("/.well-known/jwks.json", "/.well-known/public-key").permitAll()
            .requestMatchers("/.well-known/**").permitAll()
            // OAuth2相关端点
            .requestMatchers("/oauth2/**").permitAll()
            // Druid监控页面
            .requestMatchers("/druid/**").permitAll()
            // 其他所有请求都需要认证（内部服务调用通过Filter自动认证）
            .anyRequest().authenticated();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
