package com.lynn.museum.common.constants;

/**
 * API常量类
 * 
 * @author lynn
 * @since 1.0.0
 */
public class ApiConstants {

    /**
     * API版本
     */
    public static final String API_VERSION_V1 = "/v1";
    public static final String API_VERSION_V2 = "/v2";
    
    /**
     * API基础路径
     */
    public static final String API_BASE_PATH = "/api";
    public static final String API_V1_PATH = API_BASE_PATH + API_VERSION_V1;
    public static final String API_V2_PATH = API_BASE_PATH + API_VERSION_V2;
    
    /**
     * 用户相关API路径
     */
    public static final String USER_BASE_PATH = "/system";
    public static final String USER_LOGIN = "/login";
    public static final String USER_LOGOUT = "/logout";
    public static final String USER_REGISTER = "/register";
    public static final String USER_INFO = "/info";
    public static final String USER_UPDATE = "/update";
    public static final String USER_CHANGE_PASSWORD = "/change-password";
    public static final String USER_RESET_PASSWORD = "/reset-password";
    public static final String USER_PROFILE = "/profile";
    public static final String USER_AVATAR = "/avatar";
    
    /**
     * 博物馆相关API路径
     */
    public static final String MUSEUM_BASE_PATH = "/museum";
    public static final String MUSEUM_LIST = "/list";
    public static final String MUSEUM_DETAIL = "/detail";
    public static final String MUSEUM_SEARCH = "/search";
    public static final String MUSEUM_NEARBY = "/nearby";
    public static final String MUSEUM_HOT = "/hot";
    public static final String MUSEUM_RECOMMEND = "/recommend";
    public static final String MUSEUM_STATISTICS = "/statistics";
    
    /**
     * 打卡相关API路径
     */
    public static final String CHECKIN_BASE_PATH = "/checkin";
    public static final String CHECKIN_SUBMIT = "/submit";
    public static final String CHECKIN_RECORD = "/record";
    public static final String CHECKIN_HISTORY = "/history";
    public static final String CHECKIN_RANKING = "/ranking";
    public static final String CHECKIN_STATISTICS = "/statistics";
    
    /**
     * 展览相关API路径
     */
    public static final String EXHIBITION_BASE_PATH = "/exhibition";
    public static final String EXHIBITION_LIST = "/list";
    public static final String EXHIBITION_DETAIL = "/detail";
    public static final String EXHIBITION_CURRENT = "/current";
    public static final String EXHIBITION_UPCOMING = "/upcoming";
    public static final String EXHIBITION_HOT = "/hot";
    
    /**
     * 活动相关API路径
     */
    public static final String ACTIVITY_BASE_PATH = "/activity";
    public static final String ACTIVITY_LIST = "/list";
    public static final String ACTIVITY_DETAIL = "/detail";
    public static final String ACTIVITY_JOIN = "/join";
    public static final String ACTIVITY_CANCEL = "/cancel";
    public static final String ACTIVITY_PARTICIPANTS = "/participants";
    
    /**
     * 评价相关API路径
     */
    public static final String REVIEW_BASE_PATH = "/review";
    public static final String REVIEW_SUBMIT = "/submit";
    public static final String REVIEW_LIST = "/list";
    public static final String REVIEW_DELETE = "/delete";
    public static final String REVIEW_LIKE = "/like";
    
    /**
     * 积分相关API路径
     */
    public static final String POINTS_BASE_PATH = "/points";
    public static final String POINTS_BALANCE = "/balance";
    public static final String POINTS_HISTORY = "/history";
    public static final String POINTS_EXCHANGE = "/exchange";
    public static final String POINTS_RANKING = "/ranking";
    
    /**
     * 文件上传API路径
     */
    public static final String UPLOAD_BASE_PATH = "/upload";
    public static final String UPLOAD_IMAGE = "/image";
    public static final String UPLOAD_AVATAR = "/avatar";
    public static final String UPLOAD_FILE = "/file";
    
    /**
     * 系统相关API路径
     */
    public static final String SYSTEM_BASE_PATH = "/system";
    public static final String SYSTEM_CONFIG = "/config";
    public static final String SYSTEM_NOTICE = "/notice";
    public static final String SYSTEM_BANNER = "/banner";
    public static final String SYSTEM_VERSION = "/version";
    public static final String SYSTEM_HEALTH = "/health";
    
    /**
     * 验证码相关API路径
     */
    public static final String CAPTCHA_BASE_PATH = "/captcha";
    public static final String CAPTCHA_IMAGE = "/image";
    public static final String CAPTCHA_SMS = "/sms";
    public static final String CAPTCHA_EMAIL = "/email";
    public static final String CAPTCHA_VERIFY = "/verify";
    
    /**
     * 管理后台API路径
     */
    public static final String ADMIN_BASE_PATH = "/admin";
    public static final String ADMIN_LOGIN = "/login";
    public static final String ADMIN_LOGOUT = "/logout";
    public static final String ADMIN_USER_MANAGE = "/system";
    public static final String ADMIN_MUSEUM_MANAGE = "/museum";
    public static final String ADMIN_STATISTICS = "/statistics";
    
    /**
     * 请求方法常量
     */
    public static final String METHOD_GET = "GET";
    public static final String METHOD_POST = "POST";
    public static final String METHOD_PUT = "PUT";
    public static final String METHOD_DELETE = "DELETE";
    public static final String METHOD_PATCH = "PATCH";
    
    /**
     * 请求参数常量
     */
    public static final String PARAM_PAGE = "page";
    public static final String PARAM_SIZE = "size";
    public static final String PARAM_SORT = "sort";
    public static final String PARAM_ORDER = "order";
    public static final String PARAM_KEYWORD = "keyword";
    public static final String PARAM_TYPE = "type";
    public static final String PARAM_STATUS = "status";
    public static final String PARAM_START_TIME = "startTime";
    public static final String PARAM_END_TIME = "endTime";
    
    /**
     * 排序常量
     */
    public static final String ORDER_ASC = "asc";
    public static final String ORDER_DESC = "desc";
    
    /**
     * 私有构造函数，防止实例化
     */
    private ApiConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}