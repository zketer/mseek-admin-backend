package com.lynn.museum.common.constants;

/**
 * 缓存常量类
 * 
 * @author lynn
 * @since 1.0.0
 */
public class CacheConstants {

    /**
     * 缓存键前缀
     */
    public static final String CACHE_PREFIX = "museum:";
    
    /**
     * 用户相关缓存键
     */
    public static final String USER_INFO_KEY = CACHE_PREFIX + "system:info:";
    public static final String USER_TOKEN_KEY = CACHE_PREFIX + "system:token:";
    public static final String USER_POINTS_KEY = CACHE_PREFIX + "system:points:";
    public static final String USER_LEVEL_KEY = CACHE_PREFIX + "system:level:";
    public static final String USER_CHECKIN_COUNT_KEY = CACHE_PREFIX + "system:checkin:count:";
    
    /**
     * 博物馆相关缓存键
     */
    public static final String MUSEUM_INFO_KEY = CACHE_PREFIX + "museum:info:";
    public static final String MUSEUM_LIST_KEY = CACHE_PREFIX + "museum:list";
    public static final String MUSEUM_NEARBY_KEY = CACHE_PREFIX + "museum:nearby:";
    public static final String MUSEUM_HOT_KEY = CACHE_PREFIX + "museum:hot";
    public static final String MUSEUM_STATISTICS_KEY = CACHE_PREFIX + "museum:statistics:";
    
    /**
     * 打卡相关缓存键
     */
    public static final String CHECKIN_RECORD_KEY = CACHE_PREFIX + "checkin:record:";
    public static final String CHECKIN_TODAY_KEY = CACHE_PREFIX + "checkin:today:";
    public static final String CHECKIN_LIMIT_KEY = CACHE_PREFIX + "checkin:limit:";
    public static final String CHECKIN_RANKING_KEY = CACHE_PREFIX + "checkin:ranking";
    
    /**
     * 展览相关缓存键
     */
    public static final String EXHIBITION_INFO_KEY = CACHE_PREFIX + "exhibition:info:";
    public static final String EXHIBITION_LIST_KEY = CACHE_PREFIX + "exhibition:list:";
    public static final String EXHIBITION_HOT_KEY = CACHE_PREFIX + "exhibition:hot";
    
    /**
     * 活动相关缓存键
     */
    public static final String ACTIVITY_INFO_KEY = CACHE_PREFIX + "activity:info:";
    public static final String ACTIVITY_LIST_KEY = CACHE_PREFIX + "activity:list";
    public static final String ACTIVITY_PARTICIPANTS_KEY = CACHE_PREFIX + "activity:participants:";
    
    /**
     * 系统配置缓存键
     */
    public static final String SYSTEM_CONFIG_KEY = CACHE_PREFIX + "system:config";
    public static final String SYSTEM_NOTICE_KEY = CACHE_PREFIX + "system:notice";
    public static final String SYSTEM_BANNER_KEY = CACHE_PREFIX + "system:banner";
    
    /**
     * 验证码相关缓存键
     */
    public static final String CAPTCHA_KEY = CACHE_PREFIX + "captcha:";
    public static final String SMS_CODE_KEY = CACHE_PREFIX + "sms:code:";
    public static final String EMAIL_CODE_KEY = CACHE_PREFIX + "email:code:";
    
    /**
     * 限流相关缓存键
     */
    public static final String RATE_LIMIT_KEY = CACHE_PREFIX + "rate:limit:";
    public static final String API_LIMIT_KEY = CACHE_PREFIX + "api:limit:";
    
    /**
     * 缓存过期时间（单位：秒）
     */
    // 1分钟
    public static final long EXPIRE_MINUTE_1 = 60L;
    // 5分钟
    public static final long EXPIRE_MINUTE_5 = 5 * 60L;
    // 10分钟
    public static final long EXPIRE_MINUTE_10 = 10 * 60L;
    // 30分钟
    public static final long EXPIRE_MINUTE_30 = 30 * 60L;
    // 1小时
    public static final long EXPIRE_HOUR_1 = 60 * 60L;
    // 2小时
    public static final long EXPIRE_HOUR_2 = 2 * 60 * 60L;
    // 6小时
    public static final long EXPIRE_HOUR_6 = 6 * 60 * 60L;
    // 12小时
    public static final long EXPIRE_HOUR_12 = 12 * 60 * 60L;
    // 1天
    public static final long EXPIRE_DAY_1 = 24 * 60 * 60L;
    // 3天
    public static final long EXPIRE_DAY_3 = 3 * 24 * 60 * 60L;
    // 7天
    public static final long EXPIRE_DAY_7 = 7 * 24 * 60 * 60L;
    // 30天
    public static final long EXPIRE_DAY_30 = 30 * 24 * 60 * 60L;
    
    /**
     * 默认缓存过期时间配置
     */
    // 用户信息缓存2小时
    public static final long DEFAULT_USER_CACHE_EXPIRE = EXPIRE_HOUR_2;
    // 博物馆信息缓存1天
    public static final long DEFAULT_MUSEUM_CACHE_EXPIRE = EXPIRE_DAY_1;
    // 打卡记录缓存1天
    public static final long DEFAULT_CHECKIN_CACHE_EXPIRE = EXPIRE_DAY_1;
    // 展览信息缓存6小时
    public static final long DEFAULT_EXHIBITION_CACHE_EXPIRE = EXPIRE_HOUR_6;
    // 活动信息缓存1小时
    public static final long DEFAULT_ACTIVITY_CACHE_EXPIRE = EXPIRE_HOUR_1;
    // 系统配置缓存1天
    public static final long DEFAULT_CONFIG_CACHE_EXPIRE = EXPIRE_DAY_1;
    // 验证码缓存5分钟
    public static final long DEFAULT_CAPTCHA_EXPIRE = EXPIRE_MINUTE_5;
    // 短信验证码5分钟
    public static final long DEFAULT_SMS_CODE_EXPIRE = EXPIRE_MINUTE_5;
    // 限流缓存1分钟
    public static final long DEFAULT_RATE_LIMIT_EXPIRE = EXPIRE_MINUTE_1;
    
    /**
     * 缓存空值过期时间（防止缓存穿透）
     */
    // 空值缓存5分钟
    public static final long NULL_CACHE_EXPIRE = EXPIRE_MINUTE_5;
    
    /**
     * 私有构造函数，防止实例化
     */
    private CacheConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}