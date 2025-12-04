package com.lynn.museum.common.utils;

import org.springframework.util.StringUtils;

/**
 * Redis 键构建工具类
 *
 * 按照统一的 Redis 键命名规范构建键名
 * 规范格式: {service}:{module}:{type}:{identifier}:{suffix}
 *
 * @author lynn
 * @since 2024-01-15
 */
public class RedisKeyBuilder {

    // ==================== 服务名称常量 ====================
    public static final String SERVICE_AUTH = "auth";
    public static final String SERVICE_GATEWAY = "gateway";
    public static final String SERVICE_MUSEUM = "museum";
    public static final String SERVICE_USER = "user";
    public static final String SERVICE_FILE = "file";

    // ==================== 数据类型常量 ====================
    public static final String TYPE_STR = "str";
    public static final String TYPE_HASH = "hash";
    public static final String TYPE_SET = "set";
    public static final String TYPE_LIST = "list";
    public static final String TYPE_ZSET = "zset";

    // ==================== 模块常量 ====================
    public static final String MODULE_AUTH = "auth";
    public static final String MODULE_TOKEN = "token";
    public static final String MODULE_CACHE = "cache";
    public static final String MODULE_USER = "user";
    public static final String MODULE_JWKS = "jwks";
    public static final String MODULE_LOCK = "lock";
    public static final String MODULE_LIMIT = "limit";
    public static final String MODULE_STATS = "stats";
    public static final String MODULE_INFO = "info";
    public static final String MODULE_CHECKIN = "checkin";
    public static final String MODULE_SEARCH = "search";
    public static final String MODULE_ROLE = "role";
    public static final String MODULE_PERM = "perm";
    public static final String MODULE_SESSION = "session";
    public static final String MODULE_CAPTCHA = "captcha";
    public static final String MODULE_EMAIL = "email";
    public static final String MODULE_BLACKLIST = "blacklist";
    public static final String MODULE_FAVORITE = "favorite";

    // ==================== 认证服务键构建 ====================

    /**
     * 构建认证 Token 键
     * 格式: auth:token:str:{userId}
     */
    public static String buildAuthTokenKey(Long userId) {
        return String.format("%s:%s:%s:%s", SERVICE_AUTH, MODULE_TOKEN, TYPE_STR, userId);
    }

    /**
     * 构建认证 Token 元数据键
     * 格式: auth:token:hash:{tokenHash}
     */
    public static String buildAuthTokenMetaKey(String tokenHash) {
        return String.format("%s:%s:%s:%s", SERVICE_AUTH, MODULE_TOKEN, TYPE_HASH, tokenHash);
    }

    /**
     * 构建 Refresh Token 键
     * 格式: auth:token:refresh:str:{userId}
     */
    public static String buildAuthRefreshTokenKey(Long userId) {
        return String.format("%s:%s:refresh:%s:%s", SERVICE_AUTH, MODULE_TOKEN, TYPE_STR, userId);
    }

    /**
     * 构建 Refresh Token 元数据键
     * 格式: auth:token:refresh:hash:{tokenId}
     */
    public static String buildAuthRefreshTokenMetaKey(String tokenId) {
        return String.format("%s:%s:refresh:%s:%s", SERVICE_AUTH, MODULE_TOKEN, TYPE_HASH, tokenId);
    }

    /**
     * 构建用户锁定键
     * 格式: auth:user:lock:str:{username}
     */
    public static String buildAuthUserLockKey(String username) {
        return String.format("%s:%s:%s:%s:%s", SERVICE_AUTH, MODULE_USER, MODULE_LOCK, TYPE_STR, username);
    }

    /**
     * 构建登录失败计数键
     * 格式: auth:user:fail:str:{username}
     */
    public static String buildAuthUserFailKey(String username) {
        return String.format("%s:%s:fail:%s:%s", SERVICE_AUTH, MODULE_USER, TYPE_STR, username);
    }

    /**
     * 构建验证码键
     * 格式: auth:captcha:str:{key}
     */
    public static String buildAuthCaptchaKey(String key) {
        return String.format("%s:%s:%s:%s", SERVICE_AUTH, MODULE_CAPTCHA, TYPE_STR, key);
    }

    /**
     * 构建邮箱验证码键
     * 格式: auth:email:code:str:{email}
     */
    public static String buildAuthEmailCodeKey(String email) {
        return String.format("%s:%s:code:%s:%s", SERVICE_AUTH, MODULE_EMAIL, TYPE_STR, email);
    }

    /**
     * 构建验证码失败计数键
     * 格式: auth:captcha:fail:str:{captchaKey}
     */
    public static String buildAuthCaptchaFailKey(String captchaKey) {
        return String.format("%s:%s:fail:%s:%s", SERVICE_AUTH, MODULE_CAPTCHA, TYPE_STR, captchaKey);
    }

    // ==================== 网关服务键构建 ====================

    /**
     * 构建网关认证缓存键
     * 格式: gateway:auth:cache:str:{tokenHash}
     */
    public static String buildGatewayAuthCacheKey(String tokenHash) {
        return String.format("%s:%s:%s:%s:%s", SERVICE_GATEWAY, MODULE_AUTH, MODULE_CACHE, TYPE_STR, tokenHash);
    }

    /**
     * 构建网关 Token 黑名单键
     * 格式: gateway:auth:blacklist:str:{jti}
     */
    public static String buildGatewayAuthBlacklistKey(String jti) {
        return String.format("%s:%s:%s:%s:%s", SERVICE_GATEWAY, MODULE_AUTH, MODULE_BLACKLIST, TYPE_STR, jti);
    }

    /**
     * 构建网关缓存统计键
     * 格式: gateway:auth:stats:hash:metrics
     */
    public static String buildGatewayAuthStatsKey() {
        return String.format("%s:%s:%s:%s:metrics", SERVICE_GATEWAY, MODULE_AUTH, MODULE_STATS, TYPE_HASH);
    }

    /**
     * 构建网关 JWKS 缓存键
     * 格式: gateway:jwks:str:{keyId}
     */
    public static String buildGatewayJwksKey(String keyId) {
        return String.format("%s:%s:%s:%s", SERVICE_GATEWAY, MODULE_JWKS, TYPE_STR, keyId);
    }

    /**
     * 构建网关限流键
     * 格式: gateway:limit:str:{key}
     */
    public static String buildGatewayLimitKey(String key) {
        return String.format("%s:%s:%s:%s", SERVICE_GATEWAY, MODULE_LIMIT, TYPE_STR, key);
    }

    // ==================== 博物馆服务键构建 ====================

    /**
     * 构建博物馆信息缓存键
     * 格式: museum:info:str:{museumId}
     */
    public static String buildMuseumInfoKey(Long museumId) {
        return String.format("%s:%s:%s:%s", SERVICE_MUSEUM, MODULE_INFO, TYPE_STR, museumId);
    }

    /**
     * 构建城市博物馆列表键
     * 格式: museum:info:hash:{cityCode}
     */
    public static String buildMuseumCityListKey(String cityCode) {
        return String.format("%s:%s:%s:%s", SERVICE_MUSEUM, MODULE_INFO, TYPE_HASH, cityCode);
    }

    /**
     * 构建博物馆搜索缓存键
     * 格式: museum:search:str:{query}
     */
    public static String buildMuseumSearchKey(String query) {
        return String.format("%s:%s:%s:%s", SERVICE_MUSEUM, MODULE_SEARCH, TYPE_STR, query);
    }

    /**
     * 构建用户打卡记录键
     * 格式: museum:user:checkin:hash:{userId}
     */
    public static String buildMuseumUserCheckinKey(Long userId) {
        return String.format("%s:%s:%s:%s:%s", SERVICE_MUSEUM, MODULE_USER, MODULE_CHECKIN, TYPE_HASH, userId);
    }

    /**
     * 构建用户收藏博物馆键
     * 格式: museum:user:favorite:set:{userId}
     */
    public static String buildMuseumUserFavoriteKey(Long userId) {
        return String.format("%s:%s:%s:%s:%s", SERVICE_MUSEUM, MODULE_USER, MODULE_FAVORITE, TYPE_SET, userId);
    }

    /**
     * 构建用户统计信息键
     * 格式: museum:user:stats:hash:{userId}
     */
    public static String buildMuseumUserStatsKey(Long userId) {
        return String.format("%s:%s:%s:%s:%s", SERVICE_MUSEUM, MODULE_USER, MODULE_STATS, TYPE_HASH, userId);
    }

    // ==================== 用户服务键构建 ====================

    /**
     * 构建用户信息键
     * 格式: user:info:hash:{userId}
     */
    public static String buildUserInfoKey(Long userId) {
        return String.format("%s:%s:%s:%s", SERVICE_USER, MODULE_INFO, TYPE_HASH, userId);
    }

    /**
     * 构建用户角色集合键
     * 格式: user:role:set:{userId}
     */
    public static String buildUserRoleKey(Long userId) {
        return String.format("%s:%s:%s:%s", SERVICE_USER, MODULE_ROLE, TYPE_SET, userId);
    }

    /**
     * 构建用户权限集合键
     * 格式: user:perm:set:{userId}
     */
    public static String buildUserPermissionKey(Long userId) {
        return String.format("%s:%s:%s:%s", SERVICE_USER, MODULE_PERM, TYPE_SET, userId);
    }

    /**
     * 构建用户会话键
     * 格式: user:session:str:{sessionId}
     */
    public static String buildUserSessionKey(String sessionId) {
        return String.format("%s:%s:%s:%s", SERVICE_USER, MODULE_SESSION, TYPE_STR, sessionId);
    }

    /**
     * 构建在线用户集合键
     * 格式: user:session:set:active
     */
    public static String buildUserOnlineKey() {
        return String.format("%s:%s:%s:active", SERVICE_USER, MODULE_SESSION, TYPE_SET);
    }

    // ==================== 通用工具方法 ====================

    /**
     * 构建完整的键名
     *
     * @param service 服务名
     * @param module 模块名
     * @param type 数据类型
     * @param identifier 标识符
     * @param suffix 后缀（可选）
     * @return 完整的键名
     */
    public static String buildKey(String service, String module, String type, String identifier, String suffix) {
        if (StringUtils.hasText(suffix)) {
            return String.format("%s:%s:%s:%s:%s", service, module, type, identifier, suffix);
        } else {
            return String.format("%s:%s:%s:%s", service, module, type, identifier);
        }
    }

    /**
     * 构建完整的键名（无后缀版本）
     */
    public static String buildKey(String service, String module, String type, String identifier) {
        return buildKey(service, module, type, identifier, null);
    }

    /**
     * 私有构造函数，防止实例化
     */
    private RedisKeyBuilder() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}
