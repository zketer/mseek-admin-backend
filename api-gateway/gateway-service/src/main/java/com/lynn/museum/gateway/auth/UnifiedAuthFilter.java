package com.lynn.museum.gateway.auth;

import com.lynn.museum.gateway.auth.strategy.AuthStrategy;
import com.lynn.museum.gateway.config.UnifiedAuthConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 统一认证过滤器
 * 
 * 核心功能：
 * 1. 白名单路径检查
 * 2. 认证策略链执行
 * 3. 统一错误处理
 * 4. 性能监控
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnifiedAuthFilter implements GlobalFilter, Ordered {

    private final UnifiedAuthConfig authConfig;
    private final List<AuthStrategy> authStrategies;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        String method = request.getMethod().name();
        
        log.debug("Processing request: {} {}", method, path);

        // 1. 检查白名单路径
        if (isWhitelistPath(path)) {
            log.debug("Path {} is whitelisted, skipping authentication", path);
            return chain.filter(exchange);
        }

        // 2. 执行认证策略链
        long startTime = System.currentTimeMillis();
        return executeAuthStrategies(exchange, chain)
            .doOnSuccess(result -> {
                long duration = System.currentTimeMillis() - startTime;
                log.debug("Authentication completed for {} in {}ms", path, duration);
            })
            .doOnError(error -> {
                long duration = System.currentTimeMillis() - startTime;
                log.warn("Authentication failed for {} in {}ms: {}", path, duration, error.getMessage());
            });
    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhitelistPath(String path) {
        if (path == null || path.isEmpty()) {
            return false;
        }

        log.debug("【认证检查】检查白名单路径: path={}, patterns_count={}", path, 
            authConfig.getWhitelist().getPatterns().size());

        // 检查配置的白名单路径
        for (String pattern : authConfig.getWhitelist().getPatterns()) {
            boolean matches = pathMatcher.match(pattern, path);
            log.debug("【认证检查】路径匹配: path={}, pattern={}, matches={}", path, pattern, matches);
            if (matches) {
                log.info("【认证检查】白名单匹配成功: path={}, pattern={}", path, pattern);
                return true;
            }
        }
        
        log.warn("【认证检查】白名单匹配失败: path={}", path);
        return false;
    }

    /**
     * 执行认证策略链
     */
    private Mono<Void> executeAuthStrategies(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 获取启用且排序的认证策略
        List<AuthStrategy> enabledStrategies = authStrategies.stream()
            .filter(AuthStrategy::isEnabled)
            .sorted((a, b) -> Integer.compare(a.getOrder(), b.getOrder()))
            .collect(Collectors.toList());

        if (enabledStrategies.isEmpty()) {
            log.warn("No enabled authentication strategies found");
            return unauthorized(exchange, "No authentication method available");
        }

        log.debug("Executing authentication strategies: {}", 
            enabledStrategies.stream()
                .map(AuthStrategy::getStrategyName)
                .collect(Collectors.joining(", ")));

        // 按优先级依次尝试认证策略
        return tryAuthStrategies(exchange, chain, enabledStrategies, 0);
    }

    /**
     * 递归尝试认证策略
     */
    private Mono<Void> tryAuthStrategies(ServerWebExchange exchange, GatewayFilterChain chain, 
                                       List<AuthStrategy> strategies, int index) {
        if (index >= strategies.size()) {
            return unauthorized(exchange, "No valid authentication found");
        }

        AuthStrategy strategy = strategies.get(index);
        
        // 检查策略是否支持当前请求
        if (!strategy.supports(exchange)) {
            log.debug("Strategy {} does not support this request, trying next", strategy.getStrategyName());
            return tryAuthStrategies(exchange, chain, strategies, index + 1);
        }

        log.debug("Trying authentication strategy: {}", strategy.getStrategyName());
        
        return strategy.authenticate(exchange, chain)
            .doOnSuccess(result -> {
                log.debug("Authentication succeeded with strategy: {}", strategy.getStrategyName());
            })
            .onErrorResume(error -> {
                log.debug("Authentication failed with strategy {}: {}", 
                    strategy.getStrategyName(), error.getMessage());
                
                // 如果当前策略失败，尝试下一个策略
                return tryAuthStrategies(exchange, chain, strategies, index + 1);
            });
    }

    /**
     * 返回401未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");

        String body = String.format(
            "{\"code\":401,\"message\":\"Unauthorized: %s\",\"data\":null,\"timestamp\":%d}", 
            message, System.currentTimeMillis());
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        // 设置较高优先级，确保在路由过滤器之前执行
        return -100;
    }
}
