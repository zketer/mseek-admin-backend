package com.lynn.museum.common.result;

/**
 * 通用错误码枚举类
 * 错误码规范：
 * - 成功：200
 * - 客户端错误：400-499
 * - 服务端错误：500-599
 * - 系统相关错误：100000-199999
 * - 认证授权相关错误：200000-299999
 * - 用户相关错误：300000-399999
 * - 数据库相关错误：400000-499999
 * - 外部服务相关错误：500000-599999
 * - 缓存相关错误：600000-699999
 * - 消息队列相关错误：700000-799999
 * - 业务相关错误：800000+
 * @author lynn
 */
public enum ResultCode {
    /**
     * 成功状态码
     */
    SUCCESS(200, "操作成功", "Operation successful"),

    /**
     * 客户端错误 400-499
     */
    BAD_REQUEST(400, "请求参数错误", "Bad request"),
    UNAUTHORIZED(401, "未授权访问", "Unauthorized"),
    FORBIDDEN(403, "禁止访问", "Forbidden"),
    NOT_FOUND(404, "资源不存在", "Resource not found"),
    METHOD_NOT_ALLOWED(405, "请求方法不允许", "Method not allowed"),
    REQUEST_TIMEOUT(408, "请求超时", "Request timeout"),
    TOO_MANY_REQUESTS(429, "请求过于频繁", "Too many requests"),

    /**
     * 服务端错误 500-599
     */
    INTERNAL_SERVER_ERROR(500, "服务器内部错误", "Internal server error"),
    BAD_GATEWAY(502, "网关错误", "Bad gateway"),
    SERVICE_UNAVAILABLE(503, "服务不可用", "Service unavailable"),
    GATEWAY_TIMEOUT(504, "网关超时", "Gateway timeout"),

    /**
     * 系统相关错误码 100000-199999
     */
    SYSTEM_ERROR(100000, "系统异常", "System error"),
    PARAM_ERROR(100001, "参数错误", "Parameter error"),
    PARAM_MISSING(100002, "缺少必要参数", "Missing required parameter"),
    PARAM_INVALID(100003, "参数格式不正确", "Invalid parameter format"),
    DATA_NOT_FOUND(100004, "数据不存在", "Data not found"),
    DATA_ALREADY_EXISTS(100005, "数据已存在", "Data already exists"),
    DATA_CONFLICT(100006, "数据冲突", "Data conflict"),
    OPERATION_FAILED(100007, "操作失败", "Operation failed"),
    PERMISSION_DENIED(100008, "权限不足", "Permission denied"),
    RESOURCE_LOCKED(100009, "资源被锁定", "Resource locked"),
    CONCURRENT_UPDATE(100010, "并发更新冲突", "Concurrent update conflict"),
    FILE_UPLOAD_FAILED(100011, "文件上传失败", "File upload failed"),
    FILE_NOT_FOUND(100012, "文件不存在", "File not found"),
    FILE_SIZE_EXCEEDED(100013, "文件大小超出限制", "File size exceeded"),
    FILE_TYPE_NOT_SUPPORTED(100014, "文件类型不支持", "File type not supported"),
    OPERATION_NOT_SUPPORTED(100015, "操作不被支持", "Operation not supported"),
    FILE_NAME_EMPTY(100016, "文件名不能为空", "File name cannot be empty"),
    FILE_TOO_LARGE(100017, "文件过大", "File too large"),
    /**
     * 认证授权相关错误码 200000-299999
     */
    AUTH_FAILED(200000, "认证失败", "Authentication failed"),
    TOKEN_INVALID(200001, "令牌无效", "Invalid token"),
    TOKEN_EXPIRED(200002, "令牌已过期", "Token expired"),
    TOKEN_MISSING(200003, "缺少令牌", "Missing token"),
    LOGIN_FAILED(200004, "登录失败", "Login failed"),
    LOGOUT_FAILED(200005, "登出失败", "Logout failed"),
    ACCOUNT_LOCKED(200006, "账户被锁定", "Account locked"),
    ACCOUNT_DISABLED(200007, "账户已禁用", "Account disabled"),
    PASSWORD_EXPIRED(200008, "密码已过期", "Password expired"),
    CAPTCHA_ERROR(200009, "验证码错误", "Captcha error"),
    CAPTCHA_EXPIRED(200010, "验证码已过期", "Captcha expired"),

    /**
     * 用户相关错误码 300000-399999
     */
    USER_NOT_FOUND(300000, "用户不存在", "User not found"),
    USER_ALREADY_EXISTS(300001, "用户已存在", "User already exists"),
    USER_PASSWORD_ERROR(300002, "密码错误", "Password error"),
    USER_NOT_LOGIN(300003, "用户未登录", "User not logged in"),
    USER_INFO_INCOMPLETE(300004, "用户信息不完整", "User information incomplete"),
    USER_STATUS_ABNORMAL(300005, "用户状态异常", "User status abnormal"),
    USERNAME_INVALID(300006, "用户名格式不正确", "Invalid username format"),
    PASSWORD_TOO_WEAK(300007, "密码强度不够", "Password too weak"),
    EMAIL_INVALID(300008, "邮箱格式不正确", "Invalid email format"),
    PHONE_INVALID(300009, "手机号格式不正确", "Invalid phone format"),
    FAILED_TO_UPLOAD_AVATAR(300010, "上传头像失败", "Failed to upload avatar"),
    INVALID_USER_ID(300011, "无效的用户ID", "Invalid system ID"),
    INVALID_BASE64_AVATAR(300012, "无效的Base64头像", "Invalid Base64 avatar"),
    USER_EMAIL_ALREADY_EXISTS(300013, "邮箱已存在", "Email already exists"),
    USER_PHONE_ALREADY_EXISTS(300014, "手机号已存在", "Phone number already exists"),
    PHONE_ALREADY_EXISTS(300015, "手机号已存在", "Phone number already exists"),
    FAILED_TO_UPDATE_AVATAR(300016, "更新头像失败", "Failed to update avatar"),
    EXPORT_FAILED(300017, "导出失败", "Export failed"),
    IMPORT_FAILED(300018, "导入失败", "Import failed"),
    DOWNLOAD_TEMPLATE_FAILED(300019, "下载模板失败", "Download template failed"),
    USERNAME_EMAIL_MISMATCH(300020, "用户名与邮箱不匹配", "Username and email do not match"),
    OLD_PASSWORD_INCORRECT(300021, "旧密码不正确", "Old password is incorrect"),
    NEW_PASSWORD_SAME_AS_OLD(300022, "新密码不能与旧密码相同", "New password cannot be the same as old password"),
    FAILED_TO_UPDATE_PASSWORD(300023, "密码更新失败", "Failed to update password"),


    /**
     * 数据库相关错误码 400000-499999
     */
    DB_CONNECTION_ERROR(400000, "数据库连接错误", "Database connection error"),
    DB_QUERY_ERROR(400001, "数据库查询错误", "Database query error"),
    DB_UPDATE_ERROR(400002, "数据库更新错误", "Database update error"),
    DB_INSERT_ERROR(400003, "数据库插入错误", "Database insert error"),
    DB_DELETE_ERROR(400004, "数据库删除错误", "Database delete error"),
    DB_CONSTRAINT_VIOLATION(400005, "数据库约束违反", "Database constraint violation"),
    DB_DUPLICATE_KEY(400006, "数据库主键冲突", "Database duplicate key"),
    DB_TRANSACTION_ERROR(400007, "数据库事务错误", "Database transaction error"),

    /**
     * 外部服务相关错误码 500000-599999
     */
    EXTERNAL_SERVICE_ERROR(500000, "外部服务错误", "External service error"),
    EXTERNAL_SERVICE_TIMEOUT(500001, "外部服务超时", "External service timeout"),
    EXTERNAL_SERVICE_UNAVAILABLE(500002, "外部服务不可用", "External service unavailable"),
    API_CALL_FAILED(500003, "API调用失败", "API call failed"),
    API_RATE_LIMIT_EXCEEDED(500004, "API调用频率超限", "API rate limit exceeded"),

    /**
     * 缓存相关错误码 600000-699999
     */
    CACHE_ERROR(600000, "缓存错误", "Cache error"),
    CACHE_KEY_NOT_FOUND(600001, "缓存键不存在", "Cache key not found"),
    CACHE_EXPIRED(600002, "缓存已过期", "Cache expired"),
    CACHE_CONNECTION_ERROR(600003, "缓存连接错误", "Cache connection error"),

    /**
     * 消息队列相关错误码 700000-799999
     */
    MQ_SEND_ERROR(700000, "消息发送失败", "Message send failed"),
    MQ_RECEIVE_ERROR(700001, "消息接收失败", "Message receive failed"),
    MQ_CONNECTION_ERROR(700002, "消息队列连接错误", "Message queue connection error"),

    /**
     * 业务相关错误码 800000+（可根据具体业务扩展）
     */
    BUSINESS_ERROR(800000, "业务处理错误", "Business processing error"),
    
    /**
     * 博物馆相关错误码 800001-809999
     */
    MUSEUM_NOT_FOUND(800001, "博物馆不存在", "Museum not found"),
    MUSEUM_CLOSED(800002, "博物馆已关闭", "Museum is closed"),
    MUSEUM_FULL(800003, "博物馆已满员", "Museum is full"),
    MUSEUM_MAINTENANCE(800004, "博物馆维护中", "Museum under maintenance"),
    MUSEUM_ACCESS_DENIED(800005, "博物馆访问被拒绝", "Museum access denied"),
    MUSEUM_RESERVATION_REQUIRED(800006, "需要预约", "Reservation required"),
    MUSEUM_TICKET_INVALID(800007, "门票无效", "Invalid ticket"),
    MUSEUM_EXHIBITION_NOT_AVAILABLE(800008, "展览不可用", "Exhibition not available"),
    MUSEUM_GUIDE_NOT_AVAILABLE(800009, "导览不可用", "Guide not available"),
    MUSEUM_FACILITY_UNAVAILABLE(800010, "设施不可用", "Facility unavailable"),
    MUSEUM_CODE_EXISTS(800011, "博物馆编码已存在", "Museum Code already exists"),
    MUSEUM_TAG_NOT_FOUND(800012, "标签不存在", "Tag not found"),
    MUSEUM_TAG_RELATION_EXISTS(800013, "标签关系已存在", "Tag relation already exists"),
    MUSEUM_TAG_CODE_EXISTS(800014, "标签编码已存在", "Tag Code already exists"),
    MUSEUM_CATEGORY_NOT_FOUND(800015, "分类不存在", "Category not found"),
    MUSEUM_CATEGORY_RELATION_EXISTS(800016, "分类关系已存在", "Category relation already exists"),
    MUSEUM_CATEGORY_CODE_EXISTS(800017, "分类编码已存在", "Category Code already exists"),

            /**
             * 打卡相关错误码 810001-819999
             */
    CHECKIN_FAILED(810001, "打卡失败", "Check-in failed"),
    CHECKIN_DUPLICATE(810002, "重复打卡", "Duplicate check-in"),
    CHECKIN_TIME_INVALID(810003, "打卡时间无效", "Invalid check-in time"),
    CHECKIN_LOCATION_INVALID(810004, "打卡位置无效", "Invalid check-in location"),
    CHECKIN_QUOTA_EXCEEDED(810005, "打卡次数超限", "Check-in quota exceeded"),
    CHECKIN_NOT_FOUND(810006, "打卡记录不存在", "Check-in record not found"),
    CHECKIN_ALREADY_COMPLETED(810007, "已完成打卡", "Check-in already completed"),
    CHECKIN_PERMISSION_DENIED(810008, "打卡权限不足", "Check-in permission denied"),
    CHECKIN_DEVICE_ERROR(810009, "打卡设备故障", "Check-in device error"),
    CHECKIN_NETWORK_ERROR(810010, "打卡网络异常", "Check-in network error"),
    
    /**
     * 用户活动相关错误码 820001-829999
     */
    USER_ACTIVITY_NOT_FOUND(820001, "用户活动不存在", "User activity not found"),
    USER_ACTIVITY_EXPIRED(820002, "用户活动已过期", "User activity expired"),
    USER_ACTIVITY_FULL(820003, "用户活动已满员", "User activity is full"),
    USER_ACTIVITY_CANCELLED(820004, "用户活动已取消", "User activity cancelled"),
    USER_ACTIVITY_NOT_STARTED(820005, "用户活动未开始", "User activity not started"),

    /**
     * 积分奖励相关错误码 830001-839999
     */
    POINTS_INSUFFICIENT(830001, "积分不足", "Insufficient points"),
    POINTS_EXPIRED(830002, "积分已过期", "Points expired"),
    REWARD_NOT_AVAILABLE(830003, "奖励不可用", "Reward not available"),
    REWARD_ALREADY_CLAIMED(830004, "奖励已领取", "Reward already claimed"),
    REWARD_QUOTA_EXCEEDED(830005, "奖励配额超限", "Reward quota exceeded"),

    ;
    private final Integer code;
    private final String message;
    private final String messageEn;

    ResultCode(Integer code, String message, String messageEn) {
        this.code = code;
        this.message = message;
        this.messageEn = messageEn;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public String getMessageEn() {
        return messageEn;
    }

}