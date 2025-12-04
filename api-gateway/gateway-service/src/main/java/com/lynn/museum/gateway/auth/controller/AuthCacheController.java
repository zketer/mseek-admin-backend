package com.lynn.museum.gateway.auth.controller;

import com.lynn.museum.gateway.auth.cache.AuthCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

/**
 * 认证缓存管理控制器
 * 
 * 提供认证缓存的管理和监控功能：
 * 1. 缓存统计信息查询
 * 2. 用户缓存清理
 * 3. Token黑名单管理
 * 4. 缓存性能监控
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/actuator/auth-cache")
@RequiredArgsConstructor
public class AuthCacheController {

    private final AuthCacheService authCacheService;

    /**
     * 获取认证缓存统计信息
     */
    @GetMapping("/stats")
    public Mono<ResponseEntity<AuthCacheService.CacheStats>> getCacheStats() {
        log.debug("Getting auth cache statistics");
        
        return authCacheService.getCacheStats()
            .map(ResponseEntity::ok)
            .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * 清除指定用户的所有认证缓存
     */
    @DeleteMapping("/user/{userId}")
    public Mono<ResponseEntity<Map<String, Object>>> clearUserCache(@PathVariable String userId) {
        log.info("Clearing auth cache for user: {}", userId);
        
        return authCacheService.clearUserAuthCache(userId)
            .map(count -> {
                Map<String, Object> result = new HashMap<>();
                result.put("userId", userId);
                result.put("clearedEntries", count);
                result.put("success", true);
                result.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(result);
            })
            .onErrorResume(error -> {
                log.error("Failed to clear cache for user: {}", userId, error);
                Map<String, Object> result = new HashMap<>();
                result.put("userId", userId);
                result.put("success", false);
                result.put("error", error.getMessage());
                result.put("timestamp", System.currentTimeMillis());
                
                return Mono.just(ResponseEntity.internalServerError().body(result));
            });
    }

    /**
     * 将Token加入黑名单
     */
    @PostMapping("/blacklist")
    public Mono<ResponseEntity<Map<String, Object>>> addToBlacklist(
            @RequestParam String jti,
            @RequestParam(defaultValue = "3600") long expireSeconds) {
        
        log.info("Adding token to blacklist: jti={}, expireSeconds={}", jti, expireSeconds);
        
        return authCacheService.addToBlacklist(jti, expireSeconds)
            .map(success -> {
                Map<String, Object> result = new HashMap<>();
                result.put("jti", jti);
                result.put("expireSeconds", expireSeconds);
                result.put("success", success);
                result.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(result);
            })
            .onErrorResume(error -> {
                log.error("Failed to add token to blacklist: jti={}", jti, error);
                Map<String, Object> result = new HashMap<>();
                result.put("jti", jti);
                result.put("success", false);
                result.put("error", error.getMessage());
                result.put("timestamp", System.currentTimeMillis());
                
                return Mono.just(ResponseEntity.internalServerError().body(result));
            });
    }

    /**
     * 检查Token是否在黑名单中
     */
    @GetMapping("/blacklist/{jti}")
    public Mono<ResponseEntity<Map<String, Object>>> checkBlacklist(@PathVariable String jti) {
        log.debug("Checking blacklist status for token: {}", jti);
        
        return authCacheService.isTokenBlacklisted(jti)
            .map(isBlacklisted -> {
                Map<String, Object> result = new HashMap<>();
                result.put("jti", jti);
                result.put("isBlacklisted", isBlacklisted);
                result.put("timestamp", System.currentTimeMillis());
                
                return ResponseEntity.ok(result);
            })
            .onErrorReturn(ResponseEntity.internalServerError().build());
    }

    /**
     * 获取缓存健康状态
     */
    @GetMapping("/health")
    public Mono<ResponseEntity<Map<String, Object>>> getCacheHealth() {
        return authCacheService.getCacheStats()
            .map(stats -> {
                Map<String, Object> health = new HashMap<>();
                health.put("status", "UP");
                health.put("hitRate", stats.getHitRate());
                health.put("totalHits", stats.getHits());
                health.put("totalMisses", stats.getMisses());
                health.put("totalWrites", stats.getWrites());
                health.put("timestamp", System.currentTimeMillis());
                
                // 判断健康状态
                if (stats.getHitRate() < 0.5 && (stats.getHits() + stats.getMisses()) > 100) {
                    health.put("status", "WARN");
                    health.put("message", "Low cache hit rate detected");
                }
                
                return ResponseEntity.ok(health);
            })
            .onErrorResume(error -> {
                Map<String, Object> health = new HashMap<>();
                health.put("status", "DOWN");
                health.put("error", error.getMessage());
                health.put("timestamp", System.currentTimeMillis());
                
                return Mono.just(ResponseEntity.ok(health));
            });
    }
}
