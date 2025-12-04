package com.lynn.museum.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 全局日志过滤器
 * 记录所有通过网关的请求和响应信息
 * 
 * @author lynn
 * @since 2024
 */
@Slf4j
@Component
public class GlobalLoggingFilter implements GlobalFilter, Ordered {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        
        String requestId = generateRequestId();
        String startTime = LocalDateTime.now().format(FORMATTER);
        
        // 记录请求信息
        log.info("[{}] [{}] Request: {} {} from {}", 
                requestId, startTime, request.getMethod(), request.getURI(), getClientIp(request));
        
        // 记录请求头（调试模式）
        if (log.isDebugEnabled()) {
            request.getHeaders().forEach((name, values) -> 
                log.debug("[{}] Request Header: {} = {}", requestId, name, values));
        }
        
        long startTimeMillis = System.currentTimeMillis();
        
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long endTimeMillis = System.currentTimeMillis();
            long duration = endTimeMillis - startTimeMillis;
            String endTime = LocalDateTime.now().format(FORMATTER);
            
            // 记录响应信息
            log.info("[{}] [{}] Response: {} {} - Status: {} - Duration: {}ms", 
                    requestId, endTime, request.getMethod(), request.getURI(), 
                    response.getStatusCode(), duration);
            
            // 记录响应头（调试模式）
            if (log.isDebugEnabled()) {
                response.getHeaders().forEach((name, values) -> 
                    log.debug("[{}] Response Header: {} = {}", requestId, name, values));
            }
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }

    /**
     * 生成请求ID
     */
    private String generateRequestId() {
        return "REQ-" + System.currentTimeMillis() + "-" + Thread.currentThread().getId();
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(ServerHttpRequest request) {
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddress() != null 
                ? request.getRemoteAddress().getAddress().getHostAddress() 
                : "unknown";
    }
}