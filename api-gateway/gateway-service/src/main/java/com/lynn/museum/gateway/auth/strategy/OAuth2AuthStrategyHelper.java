package com.lynn.museum.gateway.auth.strategy;

import com.lynn.museum.gateway.auth.cache.AuthCacheService;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Date;
import java.util.List;

/**
 * OAuth2认证策略辅助类
 * 
 * 提供认证过程中的辅助方法：
 * 1. 用户信息提取和转换
 * 2. 请求Header设置
 * 3. 缓存数据处理
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
public class OAuth2AuthStrategyHelper {

    /**
     * 从JWT Claims中提取缓存认证信息
     */
    public static AuthCacheService.CachedAuthInfo extractCachedAuthInfo(Claims claims) {
        AuthCacheService.CachedAuthInfo authInfo = new AuthCacheService.CachedAuthInfo();
        
        authInfo.setUserId(claims.getSubject());
        authInfo.setUsername(claims.get("username", String.class));
        
        @SuppressWarnings("unchecked")
        List<String> roles = claims.get("roles", List.class);
        authInfo.setRoles(roles);
        
        @SuppressWarnings("unchecked")
        List<String> permissions = claims.get("permissions", List.class);
        authInfo.setPermissions(permissions);
        
        authInfo.setExpiresAt(Date.from(claims.getExpiration().toInstant()
            .atZone(java.time.ZoneId.systemDefault()).toInstant()));
        
        return authInfo;
    }

    /**
     * 使用缓存的认证信息继续处理请求
     */
    public static Mono<Void> proceedWithCachedUserInfo(ServerWebExchange exchange, 
                                                      GatewayFilterChain chain, 
                                                      AuthCacheService.CachedAuthInfo authInfo) {
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header("X-Auth-Type", "OAUTH2")
                .header("X-User-ID", authInfo.getUserId())
                .header("X-Username", authInfo.getUsername())
                .header("X-User-Roles", String.join(",", authInfo.getRoles() != null ? authInfo.getRoles() : List.of()))
                .header("X-User-Permissions", String.join(",", authInfo.getPermissions() != null ? authInfo.getPermissions() : List.of()))
                .header("X-Auth-Time", String.valueOf(System.currentTimeMillis()))
                // 标识来源于缓存
                .header("X-Auth-Source", "CACHE")
                .build();

        ServerWebExchange mutatedExchange = exchange.mutate()
                .request(mutatedRequest)
                .build();

        log.info("OAuth2 authentication succeeded (cached): userId={}, username={}", 
                authInfo.getUserId(), authInfo.getUsername());

        return chain.filter(mutatedExchange);
    }
}
