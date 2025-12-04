package com.lynn.museum.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.ArrayList;

/**
 * 统一认证配置
 * 
 * 集中管理所有认证相关配置：
 * 1. 认证策略配置
 * 2. 白名单配置
 * 3. OAuth2配置
 * 4. 内部服务认证配置
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "museum.auth")
public class UnifiedAuthConfig {

    /**
     * 认证策略配置
     */
    private Strategies strategies = new Strategies();
    
    /**
     * 白名单配置
     */
    private Whitelist whitelist = new Whitelist();
    
    /**
     * OAuth2配置
     */
    private OAuth2 oauth2 = new OAuth2();
    
    /**
     * JWT配置
     */
    private Jwt jwt = new Jwt();

    @Data
    public static class Strategies {
        /**
         * 内部服务认证策略
         */
        private InternalService internalService = new InternalService();
        
        /**
         * OAuth2认证策略
         */
        private OAuth2Strategy oauth2 = new OAuth2Strategy();
        
        @Data
        public static class InternalService {
            private boolean enabled = true;
            private int order = 1;
            private List<String> allowedServices = new ArrayList<>();
            private List<String> allowedIps = new ArrayList<>();
            private String headerName = "X-Internal-Call";
            private String serviceTokenHeader = "X-Service-Token";
            private String serviceIdHeader = "X-Service-ID";
        }
        
        @Data
        public static class OAuth2Strategy {
            private boolean enabled = true;
            private int order = 2;
        }
    }

    @Data
    public static class Whitelist {
        /**
         * 白名单路径模式
         */
        private List<String> patterns = new ArrayList<>();
        
        static {
            // 默认白名单路径
        }
    }

    @Data
    public static class OAuth2 {
        /**
         * 微信登录配置
         */
        private Wechat wechat = new Wechat();
        
        /**
         * 支付宝登录配置
         */
        private Alipay alipay = new Alipay();
        
        @Data
        public static class Wechat {
            private String appId;
            private String appSecret;
            private String redirectUri;
        }
        
        @Data
        public static class Alipay {
            private String appId;
            private String privateKey;
            private String publicKey;
        }
    }

    @Data
    public static class Jwt {
        // 2小时
        private long accessTokenExpire = 7200;
        // 7天
        private long refreshTokenExpire = 604800;
        private String issuer = "museum-auth";
        private String headerName = "Authorization";
        private String tokenPrefix = "Bearer ";
        
        /**
         * JWKS配置
         */
        private Jwks jwks = new Jwks();
    }
    
    @Data
    public static class Jwks {
        /**
         * JWKS缓存Key前缀
         */
        private String cachePrefix = "cache:jwk:";
        
        /**
         * JWKS缓存持续时间（小时）
         */
        private long cacheDurationHours = 1;
        
        /**
         * JWKS端点路径
         */
        private String endpoint = "/.well-known/jwks.json";
    }
}
