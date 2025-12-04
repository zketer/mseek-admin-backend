package com.lynn.museum.common.constants;

/**
 * 验证相关常量
 * 统一管理验证规则，避免硬编码
 * 
 * @author lynn
 * @since 2024-01-01
 */
public final class ValidationConstants {

    private ValidationConstants() {}

    /**
     * 用户相关验证规则
     */
    public static final class User {
        // 用户名规则
        public static final int USERNAME_MIN_LENGTH = 3;
        public static final int USERNAME_MAX_LENGTH = 20;
        public static final String USERNAME_PATTERN = "^[a-zA-Z0-9_]+$";
        public static final String USERNAME_MESSAGE = "用户名只能包含字母、数字和下划线";
        
        // 密码规则
        public static final int PASSWORD_MIN_LENGTH = 6;
        public static final int PASSWORD_MAX_LENGTH = 20;
        public static final String PASSWORD_MESSAGE = "密码长度必须在6-20个字符之间";
        
        // 昵称规则
        public static final int NICKNAME_MAX_LENGTH = 50;
        public static final String NICKNAME_MESSAGE = "昵称长度不能超过50个字符";
        
        // 手机号规则
        public static final String PHONE_PATTERN = "^1[3-9]\\d{9}$";
        public static final String PHONE_MESSAGE = "手机号格式不正确";
        
        // 性别值范围
        public static final int GENDER_MIN = 0;
        public static final int GENDER_MAX = 2;
        public static final String GENDER_MESSAGE = "性别值不正确";
        
        // 状态值范围
        public static final int STATUS_MIN = 0;
        public static final int STATUS_MAX = 1;
        public static final String STATUS_MESSAGE = "状态值不正确";
        
        // 备注长度
        public static final int REMARK_MAX_LENGTH = 500;
        public static final String REMARK_MESSAGE = "备注长度不能超过500个字符";
    }

    /**
     * 通用验证消息
     */
    public static final class Common {
        public static final String NOT_BLANK = "不能为空";
        public static final String NOT_NULL = "不能为空";
        public static final String EMAIL_FORMAT = "邮箱格式不正确";
        public static final String ID_INVALID = "ID格式不正确";
        public static final String PAGE_SIZE_INVALID = "页大小必须在1-100之间";
    }

    /**
     * 正则表达式常量
     */
    public static final class Pattern {
        // 用户名：字母数字下划线
        public static final String USERNAME = "^[a-zA-Z0-9_]+$";
        
        // 手机号：1开头的11位数字
        public static final String PHONE = "^1[3-9]\\d{9}$";
        
        // 邮箱：标准邮箱格式
        public static final String EMAIL = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        
        // 密码：至少包含字母和数字
        public static final String PASSWORD_COMPLEX = "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{6,20}$";
        
        // 纯数字
        public static final String ALL_DIGITS = "^\\d+$";
        
        // 纯字母
        public static final String ALL_LETTERS = "^[a-zA-Z]+$";
    }

    /**
     * 数值范围常量
     */
    public static final class Range {
        // 分页相关
        public static final int PAGE_SIZE_MIN = 1;
        public static final int PAGE_SIZE_MAX = 100;
        public static final int PAGE_SIZE_DEFAULT = 10;
        
        // 文件大小（MB）
        public static final long FILE_SIZE_MAX = 10;
        
        // BCrypt加密轮数
        public static final int BCRYPT_ROUNDS = 10;
        
        // 验证码相关
        public static final int CAPTCHA_LENGTH = 4;
        public static final int CAPTCHA_EXPIRE_MINUTES = 5;
    }
}
