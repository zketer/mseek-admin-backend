package com.lynn.museum.gateway.service;

// import com.lynn.museum.common.constants.ServiceConstants; // 暂时注释，避免依赖问题
import com.lynn.museum.common.utils.RedisKeyBuilder;
import com.lynn.museum.gateway.config.UnifiedAuthConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.time.Duration;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * JWKS服务
 * 负责从认证服务获取和缓存公钥
 * 
 * 功能：
 * 1. 动态获取JWKS
 * 2. 缓存公钥信息
 * 3. 支持密钥轮换
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class JwksService {
    
    private final WebClient.Builder webClientBuilder;
    private final DiscoveryClient discoveryClient;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final UnifiedAuthConfig authConfig;
    
    /**
     * 获取公钥（优先从缓存获取）
     */
    public Mono<PublicKey> getPublicKey(String keyId) {
        String cacheKey = RedisKeyBuilder.buildGatewayJwksKey(keyId);
        Duration cacheDuration = Duration.ofHours(authConfig.getJwt().getJwks().getCacheDurationHours());

        // Redis GET 操作添加重试机制
        Mono<String> redisGetOperation = redisTemplate.opsForValue().get(cacheKey)
            .cast(String.class)
            .retryWhen(Retry.backoff(3, Duration.ofMillis(500))
                .filter(throwable -> {
                    // 只对超时和连接错误重试
                    return throwable instanceof org.springframework.dao.QueryTimeoutException ||
                           throwable instanceof io.lettuce.core.RedisCommandTimeoutException ||
                           throwable.getMessage() != null &&
                           (throwable.getMessage().contains("timeout") ||
                            throwable.getMessage().contains("Connection refused"));
                })
                .doBeforeRetry(retrySignal -> log.warn("Redis GET retry attempt {} for key {}: {}",
                    retrySignal.totalRetries(), keyId, retrySignal.failure().getMessage()))
            );

        return redisGetOperation
            .flatMap(cachedKey -> {
                log.debug("Cache hit for key: {}", keyId);
                return Mono.just(parsePublicKey(cachedKey));
            })
            .switchIfEmpty(
                // 缓存未命中，从Auth服务获取
                fetchJwksFromAuthService()
                    .flatMap(jwks -> extractPublicKey(jwks, keyId))
                    .doOnNext(publicKey -> {
                        // 异步缓存公钥，同样添加重试机制
                        String encodedKey = Base64.getEncoder().encodeToString(publicKey.getEncoded());
                        redisTemplate.opsForValue()
                            .set(cacheKey, encodedKey, cacheDuration)
                            .retryWhen(Retry.backoff(2, Duration.ofMillis(300))
                                .filter(throwable -> throwable instanceof org.springframework.dao.QueryTimeoutException ||
                                                  throwable instanceof io.lettuce.core.RedisCommandTimeoutException)
                                .doBeforeRetry(retrySignal -> log.warn("Redis SET retry attempt {} for key {}",
                                    retrySignal.totalRetries(), keyId))
                            )
                            .doOnSuccess(result -> log.debug("Cached public key: {}", keyId))
                            .doOnError(error -> log.warn("Failed to cache public key after retries", error))
                            .subscribe();
                    })
            )
            .doOnError(error -> log.error("Failed to get public key: " + keyId, error));
    }
    
    /**
     * 从认证服务获取JWKS
     */
    @SuppressWarnings("unchecked")
    private Mono<Map<String, Object>> fetchJwksFromAuthService() {
        return getAuthServiceUrl()
            .flatMap(authServiceUrl -> {
                String jwksUrl = authServiceUrl + "/api/v1/auth" + authConfig.getJwt().getJwks().getEndpoint();
                log.debug("Fetching JWKS from URL: {}", jwksUrl);
                
                return webClientBuilder.build()
                    .get()
                    .uri(jwksUrl)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .map(rawMap -> (Map<String, Object>) rawMap)
                    .doOnNext(jwks -> log.debug("Successfully fetched JWKS from auth service"))
                    .doOnError(error -> log.error("Failed to fetch JWKS from {}: {}", jwksUrl, error.getMessage()))
                    .timeout(java.time.Duration.ofSeconds(5))
                    .onErrorMap(error -> new RuntimeException("JWKS fetch failed: " + error.getMessage(), error));
            });
    }
    
    /**
     * 从JWKS中提取指定的公钥
     */
    @SuppressWarnings("unchecked")
    private Mono<PublicKey> extractPublicKey(Map<String, Object> jwks, String keyId) {
        try {
            List<Map<String, Object>> keys = (List<Map<String, Object>>) jwks.get("keys");
            
            for (Map<String, Object> key : keys) {
                if (keyId.equals(key.get("kid"))) {
                    String n = (String) key.get("n");
                    String e = (String) key.get("e");
                    
                    // 构建RSA公钥
                    PublicKey publicKey = buildRSAPublicKey(n, e);
                    log.debug("Extracted public key for kid: {}", keyId);
                    return Mono.just(publicKey);
                }
            }
            
            return Mono.error(new RuntimeException("Public key not found for kid: " + keyId));
            
        } catch (Exception ex) {
            return Mono.error(new RuntimeException("Failed to extract public key", ex));
        }
    }
    
    /**
     * 构建RSA公钥
     */
    private PublicKey buildRSAPublicKey(String nBase64, String eBase64) {
        try {
            byte[] nBytes = Base64.getUrlDecoder().decode(nBase64);
            byte[] eBytes = Base64.getUrlDecoder().decode(eBase64);
            
            java.math.BigInteger modulus = new java.math.BigInteger(1, nBytes);
            java.math.BigInteger exponent = new java.math.BigInteger(1, eBytes);
            
            java.security.spec.RSAPublicKeySpec spec = new java.security.spec.RSAPublicKeySpec(modulus, exponent);
            KeyFactory factory = KeyFactory.getInstance("RSA");
            
            return factory.generatePublic(spec);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to build RSA public key", e);
        }
    }
    
    /**
     * 解析缓存的公钥
     */
    private PublicKey parsePublicKey(String encodedKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(encodedKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse cached public key", e);
        }
    }
    
    /**
     * 获取认证服务URL
     */
    private Mono<String> getAuthServiceUrl() {
        return Mono.fromCallable(() -> {
            List<ServiceInstance> instances = discoveryClient.getInstances("auth-service");
            if (instances.isEmpty()) {
                throw new RuntimeException("Auth service not available");
            }
            ServiceInstance instance = instances.get(0);
            return "http://" + instance.getHost() + ":" + instance.getPort();
        });
    }
}
