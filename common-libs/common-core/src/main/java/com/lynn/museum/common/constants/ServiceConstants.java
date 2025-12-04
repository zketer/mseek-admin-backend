package com.lynn.museum.common.constants;

/**
 * 服务相关常量
 * 统一管理服务名称、路径等常量
 * 
 * @author lynn
 * @since 2024-01-01
 */
public final class ServiceConstants {

    private ServiceConstants() {}

    /**
     * 服务名称
     */
    public static final class ServiceName {
        public static final String AUTH = "auth-service";
        public static final String USER = "user-service";
        public static final String GATEWAY = "gateway-service";
    }

    /**
     * 服务路径
     */
    public static final class Path {
        // 认证相关路径
        public static final String AUTH_LOGIN = "/auth/login";
        public static final String AUTH_REFRESH = "/auth/refresh";
        public static final String AUTH_VALIDATE = "/auth/validate";
        public static final String AUTH_LOGOUT = "/auth/logout";
        
        // JWKS相关路径
        public static final String JWKS_JSON = "/.well-known/jwks.json";
        public static final String PUBLIC_KEY = "/.well-known/public-key";
        
        // 健康检查路径
        public static final String HEALTH = "/actuator/health";
        public static final String INFO = "/actuator/info";
        
        // 文档相关路径
        public static final String SWAGGER_UI = "/swagger-ui";
        public static final String API_DOCS = "/v3/api-docs";
        public static final String DOC_HTML = "/doc.html";
    }

    /**
     * HTTP相关常量
     */
    public static final class Http {
        public static final String PROTOCOL_HTTP = "http://";
        public static final String PROTOCOL_HTTPS = "https://";
        public static final String LOCALHOST = "localhost";
        
        // 默认端口
        public static final int DEFAULT_HTTP_PORT = 80;
        public static final int DEFAULT_HTTPS_PORT = 443;
        
        // 项目默认端口
        public static final int GATEWAY_PORT = 8000;
        public static final int AUTH_PORT = 8001;
        public static final int USER_PORT = 8002;
    }

    /**
     * 缓存Key前缀
     */
    public static final class CacheKey {
        public static final String AUTH_PREFIX = "auth:";
        public static final String USER_PREFIX = "system:";
        public static final String TOKEN_PREFIX = "token:";
        public static final String JWKS_PREFIX = "jwks:";
        public static final String BLACKLIST_PREFIX = "blacklist:";
        
        // 具体缓存Key
        public static final String ACCESS_TOKEN = "access_token:";
        public static final String REFRESH_TOKEN = "refresh_token:";
        public static final String TOKEN_BLACKLIST = "token_blacklist:";
        public static final String GATEWAY_AUTH = "gateway:auth:";
        public static final String CACHE_JWK = "cache:jwk:";
    }

    /**
     * Header名称
     */
    public static final class Header {
        public static final String AUTHORIZATION = "Authorization";
        public static final String USER_ID = "X-User-Id";
        public static final String USERNAME = "X-Username";
        public static final String CLIENT_IP = "X-Real-IP";
        public static final String FORWARDED_FOR = "X-Forwarded-For";
        public static final String CONTENT_TYPE = "Content-Type";
    }

    /**
     * Content-Type常量
     */
    public static final class ContentType {
        public static final String JSON = "application/json";
        public static final String JSON_UTF8 = "application/json;charset=UTF-8";
        public static final String FORM_URLENCODED = "application/x-www-form-urlencoded";
        public static final String MULTIPART_FORM_DATA = "multipart/form-data";
    }

    /**
     * JWT相关常量
     */
    public static final class Jwt {
        public static final String ISSUER = "auth-service";
        public static final String AUDIENCE = "museum-services";
        public static final String TOKEN_PREFIX = "Bearer ";
        public static final String ALGORITHM_RS256 = "RS256";
        public static final String ALGORITHM_HS256 = "HS256";
    }
}
