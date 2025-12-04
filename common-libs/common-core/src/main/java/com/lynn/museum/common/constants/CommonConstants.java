package com.lynn.museum.common.constants;

/**
 * 通用常量类
 * 
 * @author lynn
 * @since 1.0.0
 */
public class CommonConstants {

    /**
     * 系统相关常量
     */
    public static final String SYSTEM_NAME = "博物馆打卡系统";
    public static final String SYSTEM_VERSION = "1.0.0";
    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final String DEFAULT_LOCALE = "zh_CN";
    
    /**
     * 时间格式常量
     */
    public static final String DATE_FORMAT = "yyyy-MM-dd";
    public static final String TIME_FORMAT = "HH:mm:ss";
    public static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String TIMESTAMP_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    
    /**
     * 数字常量
     */
    public static final int ZERO = 0;
    public static final int ONE = 1;
    public static final int MINUS_ONE = -1;
    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 100;
    
    /**
     * 字符串常量
     */
    public static final String EMPTY_STRING = "";
    public static final String SPACE = " ";
    public static final String COMMA = ",";
    public static final String DOT = ".";
    public static final String SLASH = "/";
    public static final String COLON = ":";
    public static final String SEMICOLON = ";";
    public static final String HYPHEN = "-";
    public static final String UNDERSCORE = "_";
    
    /**
     * 布尔值字符串常量
     */
    public static final String TRUE_STRING = "true";
    public static final String FALSE_STRING = "false";
    public static final String YES = "Y";
    public static final String NO = "N";
    
    /**
     * HTTP相关常量
     */
    public static final String CONTENT_TYPE_JSON = "application/json";
    public static final String CONTENT_TYPE_XML = "application/xml";
    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";
    public static final String CONTENT_TYPE_MULTIPART = "multipart/form-data";
    
    /**
     * 请求头常量
     */
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String HEADER_CONTENT_TYPE = "Content-Type";
    public static final String HEADER_USER_AGENT = "User-Agent";
    public static final String HEADER_ACCEPT = "Accept";
    
    /**
     * Token相关常量
     */
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String TOKEN_HEADER = "Authorization";
    // 7天
    public static final long TOKEN_EXPIRE_TIME = 7 * 24 * 60 * 60 * 1000L;
    
    /**
     * 缓存相关常量
     */
    public static final String CACHE_PREFIX = "museum:";
    // 30分钟
    public static final long DEFAULT_CACHE_EXPIRE = 30 * 60;
    // 24小时
    public static final long LONG_CACHE_EXPIRE = 24 * 60 * 60;
    
    /**
     * 私有构造函数，防止实例化
     */
    private CommonConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}