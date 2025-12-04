package com.lynn.museum.gateway.auth.cache;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lynn.museum.common.utils.RedisKeyBuilder;
import com.lynn.museum.gateway.config.UnifiedAuthConfig;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.util.Date;
import java.util.List;

/**
 * 认证缓存服务
 * 
 * 优化的认证缓存机制：
 * 1. 安全的缓存Key生成策略
 * 2. JSON序列化支持
 * 3. 缓存统计和监控
 * 4. 自动过期管理
 * 5. 避免重复认证的高性能缓存
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthCacheService {

    private final ReactiveStringRedisTemplate redisTemplate;
    private final UnifiedAuthConfig authConfig;
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 使用 RedisKeyBuilder 生成键，不再需要前缀常量

    /**
     * 检查认证缓存
     * 
     * @param token JWT Token
     * @return 缓存的认证信息，如果未命中返回null
     */
    public Mono<CachedAuthInfo> getAuthInfo(String token) {
        String cacheKey = generateCacheKey(token);
        // TODO: 迁移到新键格式 - gateway:auth:cache:str:{tokenHash}
        
        return redisTemplate.opsForValue()
            .get(cacheKey)
            .flatMap(this::deserializeAuthInfo)
            .doOnNext(authInfo -> {
                log.debug("Auth cache hit for user: {}", authInfo.getUserId());
                recordCacheHit();
            })
            .doOnError(error -> {
                log.warn("Failed to get auth cache: {}", error.getMessage());
                recordCacheMiss();
            })
            .onErrorResume(error -> Mono.empty());
    }

    /**
     * 缓存认证信息
     * 
     * @param token JWT Token
     * @param authInfo 认证信息
     * @param jti JWT ID（用于黑名单检查）
     * @return 缓存操作结果
     */
    public Mono<Boolean> cacheAuthInfo(String token, CachedAuthInfo authInfo, String jti) {
        String cacheKey = generateCacheKey(token);
        
        // 设置缓存过期时间为Token过期时间的一半，确保安全性
        Duration cacheDuration = Duration.ofSeconds(authConfig.getJwt().getAccessTokenExpire() / 2);
        
        // 添加缓存时间戳和JTI
        authInfo.setCachedAt(new Date());
        authInfo.setJti(jti);
        
        return serializeAuthInfo(authInfo)
            .flatMap(serialized -> redisTemplate.opsForValue()
                .set(cacheKey, serialized, cacheDuration))
            .doOnSuccess(result -> {
                log.debug("Cached auth info for user: {}, duration: {}s", 
                    authInfo.getUserId(), cacheDuration.getSeconds());
                recordCacheWrite();
            })
            .doOnError(error -> {
                log.error("Failed to cache auth info for user: {}", authInfo.getUserId(), error);
            })
            .onErrorReturn(false);
    }

    /**
     * 检查Token是否在黑名单中
     * 
     * @param jti JWT ID
     * @return true如果在黑名单中
     */
    public Mono<Boolean> isTokenBlacklisted(String jti) {
        if (!StringUtils.hasText(jti)) {
            return Mono.just(false);
        }

        String blacklistKey = RedisKeyBuilder.buildGatewayAuthBlacklistKey(jti);
        return redisTemplate.hasKey(blacklistKey)
            .doOnNext(isBlacklisted -> {
                if (isBlacklisted) {
                    log.warn("Token is blacklisted: {}", jti);
                }
            })
            .onErrorReturn(false);
    }

    /**
     * 将Token加入黑名单
     * 
     * @param jti JWT ID
     * @param expireTime Token过期时间（秒）
     * @return 操作结果
     */
    public Mono<Boolean> addToBlacklist(String jti, long expireTime) {
        if (!StringUtils.hasText(jti)) {
            return Mono.just(false);
        }

        String blacklistKey = RedisKeyBuilder.buildGatewayAuthBlacklistKey(jti);
        Duration expireDuration = Duration.ofSeconds(expireTime);
        
        return redisTemplate.opsForValue()
            .set(blacklistKey, "blacklisted", expireDuration)
            .doOnSuccess(result -> log.info("Added token to blacklist: {}", jti))
            .doOnError(error -> log.error("Failed to add token to blacklist: {}", jti, error))
            .onErrorReturn(false);
    }

    /**
     * 清除用户的所有认证缓存
     * 
     * @param userId 用户ID
     * @return 操作结果
     */
    public Mono<Long> clearUserAuthCache(String userId) {
        // 使用新的键格式模式：gateway:auth:cache:str:{tokenHash}
        String pattern = "gateway:auth:cache:str:*";

        return redisTemplate.keys(pattern)
            .collectList()
            .flatMap(keys -> {
                if (keys.isEmpty()) {
                    return Mono.just(0L);
                }
                return redisTemplate.delete(keys.toArray(new String[0]));
            })
            .doOnSuccess(count -> log.info("Cleared {} auth cache entries for user: {}", count, userId))
            .doOnError(error -> log.error("Failed to clear auth cache for user: {}", userId, error))
            .onErrorReturn(0L);
    }

    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计
     */
    public Mono<CacheStats> getCacheStats() {
        String statsKey = RedisKeyBuilder.buildGatewayAuthStatsKey();
        String hitKey = statsKey + ":hits";
        String missKey = statsKey + ":misses";
        String writeKey = statsKey + ":writes";

        return Mono.zip(
            redisTemplate.opsForValue().get(hitKey).defaultIfEmpty("0"),
            redisTemplate.opsForValue().get(missKey).defaultIfEmpty("0"),
            redisTemplate.opsForValue().get(writeKey).defaultIfEmpty("0")
        ).map(tuple -> {
            CacheStats stats = new CacheStats();
            stats.setHits(Long.parseLong(tuple.getT1()));
            stats.setMisses(Long.parseLong(tuple.getT2()));
            stats.setWrites(Long.parseLong(tuple.getT3()));
            stats.setHitRate(stats.getHits() / (double) (stats.getHits() + stats.getMisses()));
            return stats;
        }).onErrorReturn(new CacheStats());
    }

    /**
     * 生成安全的缓存Key
     *
     * 使用Token的SHA-256哈希作为缓存Key，避免hash冲突
     * 新格式：gateway:auth:cache:str:{tokenHash}
     */
    private String generateCacheKey(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            // 转换为十六进制字符串
            StringBuilder hexString = new StringBuilder();
            for (byte b : hashBytes) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }

            return RedisKeyBuilder.buildGatewayAuthCacheKey(hexString.toString());

        } catch (Exception e) {
            log.error("Failed to generate cache key, falling back to hashCode", e);
            return RedisKeyBuilder.buildGatewayAuthCacheKey("fallback:" + token.hashCode());
        }
    }

    /**
     * 序列化认证信息
     */
    private Mono<String> serializeAuthInfo(CachedAuthInfo authInfo) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(authInfo))
            .onErrorResume(JsonProcessingException.class, e -> {
                log.error("Failed to serialize auth info", e);
                return Mono.error(e);
            });
    }

    /**
     * 反序列化认证信息
     */
    private Mono<CachedAuthInfo> deserializeAuthInfo(String json) {
        return Mono.fromCallable(() -> objectMapper.readValue(json, CachedAuthInfo.class))
            .onErrorResume(JsonProcessingException.class, e -> {
                log.error("Failed to deserialize auth info: {}", json, e);
                return Mono.empty();
            });
    }

    /**
     * 记录缓存命中
     */
    private void recordCacheHit() {
        String hitKey = RedisKeyBuilder.buildGatewayAuthStatsKey() + ":hits";
        redisTemplate.opsForValue()
            .increment(hitKey)
            .subscribe();
    }

    /**
     * 记录缓存未命中
     */
    private void recordCacheMiss() {
        String missKey = RedisKeyBuilder.buildGatewayAuthStatsKey() + ":misses";
        redisTemplate.opsForValue()
            .increment(missKey)
            .subscribe();
    }

    /**
     * 记录缓存写入
     */
    private void recordCacheWrite() {
        String writeKey = RedisKeyBuilder.buildGatewayAuthStatsKey() + ":writes";
        redisTemplate.opsForValue()
            .increment(writeKey)
            .subscribe();
    }

    /**
     * 缓存的认证信息
     */
    @Data
    public static class CachedAuthInfo {
        private String userId;
        private String username;
        private List<String> roles;
        private List<String> permissions;
        private String jti;
        private Date cachedAt;
        private Date expiresAt;
    }

    /**
     * 缓存统计信息
     */
    @Data
    public static class CacheStats {
        private long hits = 0;
        private long misses = 0;
        private long writes = 0;
        private double hitRate = 0.0;
    }
}
