package com.lynn.museum.auth.utils;

import lombok.extern.slf4j.Slf4j;

import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA密钥对生成器
 * 用于生成JWT签名的RSA公私钥对
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
public class RSAKeyPairGenerator {
    
    private static final int KEY_SIZE = 2048;
    
    /**
     * 生成RSA密钥对
     */
    public static KeyPair generateKeyPair() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(KEY_SIZE);
            KeyPair keyPair = keyPairGenerator.generateKeyPair();
            
            log.info("RSA KeyPair generated successfully");
            log.info("Public Key: {}", Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
            log.info("Private Key: {}", Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
            
            return keyPair;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to generate RSA key pair", e);
        }
    }
    
    /**
     * 从Base64字符串加载私钥
     */
    public static PrivateKey loadPrivateKey(String base64PrivateKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PrivateKey);
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }
    
    /**
     * 从Base64字符串加载公钥
     */
    public static PublicKey loadPublicKey(String base64PublicKey) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(base64PublicKey);
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }
    
    /**
     * 生成密钥ID (kid)
     */
    public static String generateKeyId() {
        return "museum-key-" + System.currentTimeMillis();
    }
    
    public static void main(String[] args) {
        // 生成密钥对用于配置
        KeyPair keyPair = generateKeyPair();
        System.out.println("=== 请将以下配置添加到application.yml ===");
        System.out.println("jwt:");
        System.out.println("  rsa:");
        System.out.println("    private-key: " + Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()));
        System.out.println("    public-key: " + Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()));
        System.out.println("    key-id: " + generateKeyId());
    }
}
