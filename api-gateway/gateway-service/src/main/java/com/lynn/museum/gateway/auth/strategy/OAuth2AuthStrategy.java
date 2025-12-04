package com.lynn.museum.gateway.auth.strategy;

import com.lynn.museum.gateway.auth.cache.AuthCacheService;
import com.lynn.museum.gateway.config.UnifiedAuthConfig;
import com.lynn.museum.gateway.service.JwksService;
import static com.lynn.museum.gateway.auth.strategy.OAuth2AuthStrategyHelper.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import io.jsonwebtoken.*;
import java.security.PublicKey;
import java.time.Duration;
import java.util.List;

/**
 * OAuth 2.0 认证策略
 * 
 * 支持标准的OAuth 2.0 Bearer Token认证：
 * 1. JWT Token验证
 * 2. Token黑名单检查
 * 3. 用户信息提取
 * 4. 缓存优化
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthStrategy implements AuthStrategy {

    private final UnifiedAuthConfig authConfig;
    private final JwksService jwksService;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final AuthCacheService authCacheService;

    @Override
    public boolean supports(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(authConfig.getJwt().getHeaderName());
        
        return StringUtils.hasText(authHeader) && 
               authHeader.startsWith(authConfig.getJwt().getTokenPrefix());
    }

    @Override
    public Mono<Void> authenticate(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String authHeader = request.getHeaders().getFirst(authConfig.getJwt().getHeaderName());
        
        log.debug("Executing OAuth2 authentication strategy");

        if (!StringUtils.hasText(authHeader) || !authHeader.startsWith(authConfig.getJwt().getTokenPrefix())) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }

        String token = authHeader.substring(authConfig.getJwt().getTokenPrefix().length());

        // 先检查认证缓存
        return authCacheService.getAuthInfo(token)
            .flatMap(cachedAuthInfo -> {
                log.debug("Using cached authentication info for user: {}", cachedAuthInfo.getUserId());
                return proceedWithCachedUserInfo(exchange, chain, cachedAuthInfo);
            })
            .switchIfEmpty(
                // 缓存未命中，执行完整的Token验证
                validateToken(exchange, chain, token)
            )
            .onErrorResume(error -> {
                log.warn("OAuth2 authentication failed: {}", error.getMessage());
                return unauthorized(exchange, "Authentication failed");
            });
    }

    /**
     * 验证JWT Token
     */
    private Mono<Void> validateToken(ServerWebExchange exchange, GatewayFilterChain chain, String token) {
        try {
            // 解析JWT头部获取kid
            String[] tokenParts = token.split("\\.");
            if (tokenParts.length != 3) {
                return unauthorized(exchange, "Invalid token format");
            }

            String headerJson = new String(java.util.Base64.getUrlDecoder().decode(tokenParts[0]));
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode headerNode = mapper.readTree(headerJson);
            String keyId = headerNode.get("kid").asText();

            if (!StringUtils.hasText(keyId)) {
                return unauthorized(exchange, "Missing key ID in token");
            }

            // 获取公钥并验证Token
            return jwksService.getPublicKey(keyId)
                .flatMap(publicKey -> validateTokenWithPublicKey(token, publicKey))
                .flatMap(claims -> {
                    // 检查Token黑名单
                    String jti = claims.getId();
                    return authCacheService.isTokenBlacklisted(jti)
                        .flatMap(isBlacklisted -> {
                            if (isBlacklisted) {
                                log.warn("Token is blacklisted: {}", jti);
                                return unauthorized(exchange, "Token has been revoked");
                            }

                            // 提取用户信息
                            AuthCacheService.CachedAuthInfo authInfo = extractCachedAuthInfo(claims);
                            
                            // 异步缓存认证信息
                            authCacheService.cacheAuthInfo(token, authInfo, jti).subscribe();
                            
                            return proceedWithCachedUserInfo(exchange, chain, authInfo);
                        });
                })
                .onErrorResume(error -> {
                    log.error("JWT validation error: {}", error.getMessage());
                    return unauthorized(exchange, "Token validation failed");
                });

        } catch (Exception e) {
            log.error("JWT parsing error", e);
            return unauthorized(exchange, "Token parsing failed");
        }
    }

    /**
     * 使用公钥验证JWT Token
     */
    private Mono<Claims> validateTokenWithPublicKey(String token, PublicKey publicKey) {
        return Mono.fromCallable(() -> {
            try {
                return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .requireIssuer(authConfig.getJwt().getIssuer())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            } catch (ExpiredJwtException e) {
                throw new RuntimeException("Token expired", e);
            } catch (JwtException e) {
                throw new RuntimeException("Invalid token", e);
            }
        });
    }

    // checkTokenBlacklist方法已移动到AuthCacheService中

    // extractUserInfo方法已重构为extractCachedAuthInfo并移动到OAuth2AuthStrategyHelper中

    // cacheUserInfo方法已重构并移动到AuthCacheService中

    // proceedWithUserInfo方法已重构为proceedWithCachedUserInfo并移动到OAuth2AuthStrategyHelper中

    // 序列化和反序列化方法已重构为JSON格式并移动到AuthCacheService中

    /**
     * 返回401未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");

        String body = String.format(
            "{\"code\":401,\"message\":\"OAuth2 Auth Failed: %s\",\"data\":null,\"timestamp\":%d}", 
            message, System.currentTimeMillis());
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return authConfig.getStrategies().getOauth2().getOrder();
    }

    @Override
    public String getStrategyName() {
        return "OAuth2Auth";
    }

    @Override
    public boolean isEnabled() {
        return authConfig.getStrategies().getOauth2().isEnabled();
    }

    // UserAuthInfo类已重构为CachedAuthInfo并移动到AuthCacheService中
}
