package com.lynn.museum.common.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * 通用安全配置基础类
 * 
 * 为微服务提供统一的安全配置：
 * 1. 禁用CSRF（REST API不需要）
 * 2. 无状态会话管理
 * 3. 统一的访问控制策略
 * 4. 内部服务调用白名单支持
 * 
 * 使用方式：
 * 继承此类并根据服务特点重写configureAuthorization方法
 * 
 * @author lynn
 * @since 2024-01-01
 */
public abstract class BaseSecurityConfig {

    /**
     * 统一的密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 内部服务认证过滤器
     * 
     * 自动检查请求Header中的内部服务调用标识，无需硬编码URL路径
     */
    @Bean
    public InternalServiceAuthenticationFilter internalServiceAuthenticationFilter() {
        return new InternalServiceAuthenticationFilter();
    }
    
    /**
     * 通用安全过滤器链配置
     * 
     * 所有微服务统一的基础安全策略：
     * - 禁用CSRF保护（REST API场景）
     * - 无状态会话管理（JWT Token认证）
     * - 自动支持内部服务调用认证（基于Header检查）
     * - 允许通用的监控和文档端点
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护（对于REST API通常不需要）
            .csrf(csrf -> csrf.disable())
            // 设置会话管理为无状态（JWT Token认证）
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            // 添加内部服务认证过滤器 - 在用户名密码认证之前执行
            .addFilterBefore(internalServiceAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            // 配置请求授权 - 调用子类的具体实现
            .authorizeHttpRequests(authz -> {
                // 通用的监控和文档端点
                authz
                    .requestMatchers("/actuator/**").permitAll()
                    .requestMatchers("/health", "/info").permitAll()
                    .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/v3/api-docs/**").permitAll()
                    .requestMatchers("/doc.html", "/webjars/**", "/swagger-resources/**").permitAll()
                    .requestMatchers("/favicon.ico", "/error").permitAll();
                
                // 调用子类的特定授权配置
                configureAuthorization(authz);
            });
        
        return http.build();
    }

    /**
     * 子类实现的特定授权配置
     * 
     * @param authz 授权配置构建器
     */
    protected abstract void configureAuthorization(
        org.springframework.security.config.annotation.web.configurers.AuthorizeHttpRequestsConfigurer<HttpSecurity>.AuthorizationManagerRequestMatcherRegistry authz
    );
}
