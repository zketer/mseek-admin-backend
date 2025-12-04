package com.lynn.museum.gateway.auth.strategy;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 认证策略接口
 * 
 * 支持多种认证方式的策略模式实现：
 * 1. 内部服务认证策略
 * 2. OAuth 2.0认证策略  
 * 3. JWT认证策略
 * 4. API Key认证策略
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface AuthStrategy {
    
    /**
     * 是否支持当前请求的认证方式
     * 
     * @param exchange 当前请求交换对象
     * @return true 如果支持当前认证方式
     */
    boolean supports(ServerWebExchange exchange);
    
    /**
     * 执行认证逻辑
     * 
     * @param exchange 当前请求交换对象
     * @param chain Gateway过滤器链
     * @return 认证结果 Mono
     */
    Mono<Void> authenticate(ServerWebExchange exchange, GatewayFilterChain chain);
    
    /**
     * 策略优先级 (数值越小优先级越高)
     * 
     * @return 优先级数值
     */
    default int getOrder() {
        return Integer.MAX_VALUE;
    }
    
    /**
     * 策略名称
     * 
     * @return 策略名称，用于日志和配置
     */
    String getStrategyName();
    
    /**
     * 是否启用该策略
     * 
     * @return true 如果策略启用
     */
    default boolean isEnabled() {
        return true;
    }
}
