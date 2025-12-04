package com.lynn.museum.auth.utils;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import com.lynn.museum.common.utils.RedisKeyBuilder;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 高级JWT工具类
 * 支持RS256非对称加密、JTI黑名单、令牌元数据管理
 * 
 * 特性：
 * 1. RS256非对称加密，支持分布式验证
 * 2. JTI黑名单机制，支持令牌吊销
 * 3. 令牌元数据缓存，提升性能
 * 4. Refresh Token机制
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Component
public class AdvancedJwtUtils {
    
    private final StringRedisTemplate redisTemplate;
    
    // RSA密钥配置
    @Value("${jwt.rsa.private-key}")
    private String privateKeyBase64;
    
    @Value("${jwt.rsa.public-key}")
    private String publicKeyBase64;
    
    @Value("${jwt.rsa.key-id}")
    private String keyId;
    
    // JWT配置
    /**
     * 访问令牌有效期（毫秒）
     * 默认1小时
     */
    @Value("${jwt.access-token-expiration:3600000}")
    private long accessTokenExpiration;
    
    /**
     * 刷新令牌有效期（毫秒）
     * 默认7天
     */
    @Value("${jwt.refresh-token-expiration:604800000}")
    private long refreshTokenExpiration;
    
    /**
     * JWT颁发者
     * 默认auth-service
     */
    @Value("${jwt.issuer:auth-service}")
    private String issuer;
    
    // Redis键前缀 - 使用新的命名规范
    private static final String TOKEN_BLACKLIST_PREFIX = "auth:token:blacklist:str:";
    
    public AdvancedJwtUtils(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    /**
     * 生成访问令牌
     */
    public String generateAccessToken(Long userId, String username, Map<String, Object> claims) {
        try {
            PrivateKey privateKey = RSAKeyPairGenerator.loadPrivateKey(privateKeyBase64);
            String jti = UUID.randomUUID().toString();
            
            Date now = new Date();
            Date expiration = new Date(now.getTime() + accessTokenExpiration);
            
            // 构建JWT
            JwtBuilder builder = Jwts.builder()
                    // 密钥ID
                .setHeaderParam("kid", keyId)
                .setHeaderParam("alg", "RS256")
                .setIssuer(issuer)
                .setSubject(username)
                .setAudience("museum-services")
                .setIssuedAt(now)
                .setExpiration(expiration)
                    // JTI用于黑名单
                .setId(jti)
                .claim("userId", userId)
                .claim("username", username);
            
            // 添加自定义声明
            if (claims != null && !claims.isEmpty()) {
                claims.forEach(builder::claim);
            }
            
            String token = builder.signWith(privateKey, SignatureAlgorithm.RS256).compact();
            
            // 缓存令牌元数据
            cacheTokenMetadata(jti, userId, username, expiration);
            
            log.debug("Generated access token for system: {} (ID: {}), JTI: {}", username, userId, jti);
            return token;
            
        } catch (Exception e) {
            log.error("Failed to generate access token", e);
            throw new RuntimeException("Token generation failed", e);
        }
    }
    
    /**
     * 生成刷新令牌（使用默认过期时间）
     */
    public String generateRefreshToken(Long userId, String username) {
        return generateRefreshToken(userId, username, refreshTokenExpiration);
    }
    
    /**
     * 生成刷新令牌（自定义过期时间）
     * @param userId 用户ID
     * @param username 用户名
     * @param expirationMillis 过期时间（毫秒）
     */
    public String generateRefreshToken(Long userId, String username, long expirationMillis) {
        try {
            String refreshTokenId = UUID.randomUUID().toString();
            Date now = new Date();
            Date expiration = new Date(now.getTime() + expirationMillis);
            
            // 使用HMAC-SHA256签名刷新令牌（更轻量）
            SecretKey key = Keys.hmacShaKeyFor(privateKeyBase64.getBytes(StandardCharsets.UTF_8));
            
            String refreshToken = Jwts.builder()
                .setIssuer(issuer)
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .setId(refreshTokenId)
                .claim("userId", userId)
                .claim("type", "refresh")
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
            
            // 缓存刷新令牌
            cacheRefreshToken(userId, refreshTokenId, expiration);
            
            log.debug("Generated refresh token for system: {} (ID: {}), expiration: {}ms", username, userId, expirationMillis);
            return refreshToken;
            
        } catch (Exception e) {
            log.error("Failed to generate refresh token", e);
            throw new RuntimeException("Refresh token generation failed", e);
        }
    }
    
    /**
     * 验证访问令牌
     */
    public Claims validateAccessToken(String token) {
        try {
            PublicKey publicKey = RSAKeyPairGenerator.loadPublicKey(publicKeyBase64);
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(token)
                .getBody();
            
            String jti = claims.getId();
            
            // 检查黑名单
            if (isTokenBlacklisted(jti)) {
                log.warn("Token is blacklisted: {}", jti);
                throw new JwtException("Token has been revoked");
            }
            
            return claims;
            
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            throw e;
        } catch (JwtException e) {
            log.warn("Invalid token: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 验证刷新令牌
     */
    public Claims validateRefreshToken(String refreshToken) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(privateKeyBase64.getBytes(StandardCharsets.UTF_8));
            
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .requireIssuer(issuer)
                .build()
                .parseClaimsJws(refreshToken)
                .getBody();
            
            // 验证令牌类型
            if (!"refresh".equals(claims.get("type"))) {
                throw new JwtException("Invalid refresh token type");
            }
            
            Long userId = Long.valueOf(claims.get("userId").toString());
            String refreshTokenId = claims.getId();
            
            // 检查刷新令牌是否有效
            if (!isRefreshTokenValid(userId, refreshTokenId)) {
                throw new JwtException("Refresh token has been revoked");
            }
            
            return claims;
            
        } catch (JwtException e) {
            log.warn("Invalid refresh token: {}", e.getMessage());
            throw e;
        }
    }
    
    /**
     * 吊销访问令牌（加入黑名单）
     */
    public void revokeAccessToken(String token) {
        try {
            Claims claims = validateAccessToken(token);
            String jti = claims.getId();
            Date expiration = claims.getExpiration();
            
            // 计算剩余有效期
            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                // 添加到黑名单，TTL为剩余有效期
                redisTemplate.opsForValue().set(
                    TOKEN_BLACKLIST_PREFIX + jti, 
                    "revoked", 
                    ttl, 
                    TimeUnit.MILLISECONDS
                );
                
                log.info("Token revoked: {}", jti);
            }
            
        } catch (Exception e) {
            log.error("Failed to revoke token", e);
        }
    }
    
    /**
     * 吊销刷新令牌
     */
    public void revokeRefreshToken(Long userId, String refreshTokenId) {
        try {
            String key = RedisKeyBuilder.buildAuthRefreshTokenMetaKey(refreshTokenId);
            redisTemplate.delete(key);
            log.info("Refresh token revoked for system: {}", userId);
        } catch (Exception e) {
            log.error("Failed to revoke refresh token", e);
        }
    }
    
    /**
     * 刷新访问令牌
     */
    public Map<String, String> refreshAccessToken(String refreshToken) {
        try {
            Claims claims = validateRefreshToken(refreshToken);
            Long userId = Long.valueOf(claims.get("userId").toString());
            String username = claims.getSubject();
            
            // 生成新的访问令牌
            String newAccessToken = generateAccessToken(userId, username, null);
            
            // 可选：生成新的刷新令牌（滚动刷新）
            String newRefreshToken = generateRefreshToken(userId, username);
            
            // 吊销旧的刷新令牌
            revokeRefreshToken(userId, claims.getId());
            
            Map<String, String> tokens = new HashMap<>();
            tokens.put("accessToken", newAccessToken);
            tokens.put("refreshToken", newRefreshToken);
            
            log.info("令牌刷新成功，用户: {} (ID: {})", username, userId);
            return tokens;
            
        } catch (Exception e) {
            log.error("刷新令牌失败: {}", e.getMessage(), e);
            throw new RuntimeException("Token refresh failed: " + e.getMessage(), e);
        }
    }
    
    /**
     * 缓存令牌元数据
     */
    private void cacheTokenMetadata(String jti, Long userId, String username, Date expiration) {
        try {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("userId", userId.toString());
            metadata.put("username", username);
            metadata.put("expiration", expiration.getTime() + "");
            metadata.put("createdAt", System.currentTimeMillis() + "");

            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                String key = RedisKeyBuilder.buildAuthTokenMetaKey(jti);
                redisTemplate.opsForHash().putAll(key, metadata);
                redisTemplate.expire(key, Duration.ofMillis(ttl));
            }
        } catch (Exception e) {
            log.warn("Failed to cache token metadata", e);
        }
    }
    
    /**
     * 缓存刷新令牌
     */
    private void cacheRefreshToken(Long userId, String refreshTokenId, Date expiration) {
        try {
            String key = RedisKeyBuilder.buildAuthRefreshTokenMetaKey(refreshTokenId);
            Map<String, String> data = new HashMap<>();
            data.put("userId", userId.toString());
            data.put("expiration", expiration.getTime() + "");
            data.put("createdAt", System.currentTimeMillis() + "");

            long ttl = expiration.getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForHash().putAll(key, data);
                redisTemplate.expire(key, Duration.ofMillis(ttl));
            }
        } catch (Exception e) {
            log.warn("Failed to cache refresh token", e);
        }
    }
    
    /**
     * 检查令牌是否在黑名单中
     */
    private boolean isTokenBlacklisted(String jti) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + jti));
        } catch (Exception e) {
            log.warn("Failed to check token blacklist", e);
            // 缓存失败时允许通过
            return false;
        }
    }
    
    /**
     * 检查刷新令牌是否有效
     */
    private boolean isRefreshTokenValid(Long userId, String refreshTokenId) {
        try {
            String key = RedisKeyBuilder.buildAuthRefreshTokenMetaKey(refreshTokenId);
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.warn("Failed to check refresh token validity", e);
            return false;
        }
    }
    
    /**
     * 从令牌中提取用户ID
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = validateAccessToken(token);
            return Long.valueOf(claims.get("userId").toString());
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 从令牌中提取用户名
     */
    public String getUsernameFromToken(String token) {
        try {
            Claims claims = validateAccessToken(token);
            return claims.getSubject();
        } catch (Exception e) {
            return null;
        }
    }
    
    /**
     * 获取公钥（用于JWKS端点）
     */
    public PublicKey getPublicKey() {
        return RSAKeyPairGenerator.loadPublicKey(publicKeyBase64);
    }
    
    /**
     * 获取密钥ID
     */
    public String getKeyId() {
        return keyId;
    }
}
