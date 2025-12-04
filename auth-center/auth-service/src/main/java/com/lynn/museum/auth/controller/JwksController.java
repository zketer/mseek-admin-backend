package com.lynn.museum.auth.controller;

import com.lynn.museum.auth.utils.AdvancedJwtUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWKS (JSON Web Key Set) 控制器
 * 提供JWT验证所需的公钥信息
 * 
 * 符合RFC 7517标准，支持：
 * 1. 公钥分发
 * 2. 密钥轮换
 * 3. 多密钥支持
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "JwksController", description = "JSON Web Key Set - JWT公钥分发")
@RestController
@RequestMapping("/.well-known")
@RequiredArgsConstructor
@Slf4j
public class JwksController {
    
    private final AdvancedJwtUtils jwtUtils;
    
    @Operation(
        summary = "获取JWKS", 
        description = "返回用于验证JWT的公钥集合，符合RFC 7517标准"
    )
    @GetMapping("/jwks.json")
    public Map<String, Object> getJwks() {
        try {
            PublicKey publicKey = jwtUtils.getPublicKey();
            RSAPublicKey rsaPublicKey = (RSAPublicKey) publicKey;
            
            // 构建JWK (JSON Web Key)
            Map<String, Object> jwk = new HashMap<>();
            // Key Type
            jwk.put("kty", "RSA");
            // Public Key Use
            jwk.put("use", "sig");
            // Algorithm
            jwk.put("alg", "RS256");
            // Key ID
            jwk.put("kid", jwtUtils.getKeyId());
            
            // RSA公钥参数
            // Modulus
            jwk.put("n", Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rsaPublicKey.getModulus().toByteArray()));
            // Exponent
            jwk.put("e", Base64.getUrlEncoder().withoutPadding()
                .encodeToString(rsaPublicKey.getPublicExponent().toByteArray()));
            
            // 构建JWKS响应
            Map<String, Object> jwks = new HashMap<>();
            jwks.put("keys", List.of(jwk));
            
            log.debug("JWKS endpoint accessed, returning public key with kid: {}", jwtUtils.getKeyId());
            return jwks;
            
        } catch (Exception e) {
            log.error("Failed to generate JWKS", e);
            throw new RuntimeException("JWKS generation failed", e);
        }
    }
    
    @Operation(
        summary = "获取公钥信息", 
        description = "返回当前使用的公钥信息（调试用）"
    )
    @GetMapping("/public-key")
    public Map<String, Object> getPublicKeyInfo() {
        try {
            PublicKey publicKey = jwtUtils.getPublicKey();
            
            Map<String, Object> keyInfo = new HashMap<>();
            keyInfo.put("algorithm", publicKey.getAlgorithm());
            keyInfo.put("format", publicKey.getFormat());
            keyInfo.put("keyId", jwtUtils.getKeyId());
            keyInfo.put("publicKey", Base64.getEncoder().encodeToString(publicKey.getEncoded()));
            
            return keyInfo;
            
        } catch (Exception e) {
            log.error("Failed to get public key info", e);
            throw new RuntimeException("Public key info retrieval failed", e);
        }
    }
}
