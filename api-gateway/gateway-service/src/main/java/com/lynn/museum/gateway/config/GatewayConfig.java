package com.lynn.museum.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * 企业级网关配置类
 * 
 * Spring Cloud Gateway 配置说明：
 * 1. 声明式路由：通过application.yml配置路由规则，无需编程
 * 2. 服务发现：自动发现Nacos中注册的微服务
 * 3. 负载均衡：使用lb://协议实现客户端负载均衡
 * 4. 限流控制：基于Redis的分布式限流
 * 5. 过滤器链：请求/响应的统一处理
 * 
 * 企业级特性：
 * - 高可用：支持集群部署
 * - 高性能：基于Netty的异步非阻塞架构
 * - 可扩展：插件化的过滤器机制
 * - 可观测：集成监控和链路追踪
 * 
 * @author lynn
 * @version 1.0
 * @since 2024
 */
@Slf4j
@Configuration
public class GatewayConfig {

    /**
     * 限流键解析器 - 基于IP地址
     * 
     * Spring Cloud Gateway 限流机制：
     * 1. 基于Redis + Lua脚本实现分布式限流
     * 2. 使用令牌桶算法控制请求频率
     * 3. KeyResolver决定限流的维度（IP、用户、API等）
     * 
     * 限流算法说明：
     * - replenishRate：令牌桶填充速率（每秒放入的令牌数）
     * - burstCapacity：令牌桶容量（最大突发请求数）
     * - requestedTokens：每次请求消耗的令牌数（默认1）
     * 
     * @Primary 注解表示这是默认的KeyResolver
     * 当有多个KeyResolver时，优先使用此实现
     * 
     * @return KeyResolver 返回响应式的键解析器
     */
    @Bean
    @Primary
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            // 获取客户端真实IP地址
            String clientIp = getClientIp(exchange);
            log.debug("Rate limiting key: {}", clientIp);
            // 返回Mono包装的IP地址作为限流键
            // Mono是Project Reactor中的响应式类型，表示0或1个元素的异步序列
            return Mono.just(clientIp);
        };
    }

    /**
     * 限流键解析器 - 基于用户ID
     * 
     * 用户级限流策略：
     * 1. 优先从请求头X-User-Id获取用户标识
     * 2. 如果没有用户ID，降级为IP限流
     * 3. 适用于需要对不同用户设置不同限流策略的场景
     * 
     * 使用场景：
     * - VIP用户更高的请求配额
     * - 普通用户标准限流
     * - 恶意用户更严格的限制
     * 
     * 集成方式：
     * - 前端：在请求头中添加用户标识
     * - 认证服务：JWT Token中包含用户ID
     * - 网关：从Token解析或直接从Header获取
     * 
     * @return KeyResolver 用户维度的键解析器
     */
    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            // 从请求头获取用户ID
            // 通常由前端或认证服务在请求中添加
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            
            if (userId == null || userId.isEmpty()) {
                // 降级策略：如果没有用户ID，使用IP地址
                // 确保未认证用户也能被正确限流
                userId = getClientIp(exchange);
                log.debug("No system ID found, fallback to IP: {}", userId);
            }
            
            log.debug("User rate limiting key: {}", userId);
            return Mono.just(userId);
        };
    }

    /**
     * 获取客户端真实IP地址
     * 
     * 在企业级应用中，客户端请求通常经过多层代理：
     * 客户端 -> CDN -> 负载均衡器 -> 反向代理(Nginx) -> 网关 -> 微服务
     * 
     * IP获取优先级：
     * 1. X-Forwarded-For：标准的代理IP传递头，可能包含多个IP（客户端IP,代理IP1,代理IP2）
     * 2. X-Real-IP：Nginx等反向代理设置的真实客户端IP
     * 3. RemoteAddress：直连情况下的客户端地址
     * 
     * 安全考虑：
     * - X-Forwarded-For可能被伪造，生产环境需要验证代理链的可信性
     * - 建议在可信代理（如Nginx）中设置X-Real-IP
     * - 对于安全敏感的限流，可结合多种标识符
     * 
     * @param exchange ServerWebExchange 服务器Web交换对象，包含请求和响应信息
     * @return String 客户端IP地址，获取失败时返回"unknown"
     */
    private String getClientIp(ServerWebExchange exchange) {
        // 1. 优先检查X-Forwarded-For头
        // 格式：X-Forwarded-For: client, proxy1, proxy2
        // 第一个IP是真实客户端IP
        String xForwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            // 取第一个IP地址（真实客户端IP）
            return xForwardedFor.split(",")[0].trim();
        }
        
        // 2. 检查X-Real-IP头（通常由Nginx设置）
        String xRealIp = exchange.getRequest().getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        // 3. 最后使用直连地址（无代理情况）
        return exchange.getRequest().getRemoteAddress() != null 
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
    }
}