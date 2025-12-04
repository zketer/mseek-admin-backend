package com.lynn.museum.common.constants;

/**
 * 博物馆业务常量类
 * 
 * @author lynn
 * @since 1.0.0
 */
public class MuseumConstants {

    /**
     * 博物馆状态常量
     */
    // 正常营业
    public static final int MUSEUM_STATUS_ACTIVE = 1;
    // 暂停营业
    public static final int MUSEUM_STATUS_INACTIVE = 0;
    // 维护中
    public static final int MUSEUM_STATUS_MAINTENANCE = 2;
    // 永久关闭
    public static final int MUSEUM_STATUS_CLOSED = 3;
    
    /**
     * 博物馆类型常量
     */
    // 历史博物馆
    public static final String MUSEUM_TYPE_HISTORY = "HISTORY";
    // 艺术博物馆
    public static final String MUSEUM_TYPE_ART = "ART";
    // 科学博物馆
    public static final String MUSEUM_TYPE_SCIENCE = "SCIENCE";
    // 自然博物馆
    public static final String MUSEUM_TYPE_NATURE = "NATURE";
    // 军事博物馆
    public static final String MUSEUM_TYPE_MILITARY = "MILITARY";
    // 民俗博物馆
    public static final String MUSEUM_TYPE_FOLK = "FOLK";
    // 综合博物馆
    public static final String MUSEUM_TYPE_COMPREHENSIVE = "COMPREHENSIVE";
    
    /**
     * 打卡状态常量
     */
    // 打卡成功
    public static final int CHECKIN_STATUS_SUCCESS = 1;
    // 打卡失败
    public static final int CHECKIN_STATUS_FAILED = 0;
    // 重复打卡
    public static final int CHECKIN_STATUS_DUPLICATE = 2;
    // 无效打卡
    public static final int CHECKIN_STATUS_INVALID = 3;
    
    /**
     * 打卡类型常量
     */
    // 入馆打卡
    public static final String CHECKIN_TYPE_ENTRANCE = "ENTRANCE";
    // 出馆打卡
    public static final String CHECKIN_TYPE_EXIT = "EXIT";
    // 展览打卡
    public static final String CHECKIN_TYPE_EXHIBITION = "EXHIBITION";
    // 活动打卡
    public static final String CHECKIN_TYPE_ACTIVITY = "ACTIVITY";
    
    /**
     * 用户等级常量
     */
    // 青铜会员
    public static final int USER_LEVEL_BRONZE = 1;
    // 白银会员
    public static final int USER_LEVEL_SILVER = 2;
    // 黄金会员
    public static final int USER_LEVEL_GOLD = 3;
    // 铂金会员
    public static final int USER_LEVEL_PLATINUM = 4;
    // 钻石会员
    public static final int USER_LEVEL_DIAMOND = 5;
    
    /**
     * 积分相关常量
     */
    // 基础打卡积分
    public static final int POINTS_CHECKIN_BASIC = 10;
    // 首次打卡积分
    public static final int POINTS_CHECKIN_FIRST = 50;
    // 观看展览积分
    public static final int POINTS_EXHIBITION_VIEW = 20;
    // 参与活动积分
    public static final int POINTS_ACTIVITY_PARTICIPATE = 30;
    // 提交评价积分
    public static final int POINTS_REVIEW_SUBMIT = 15;
    
    /**
     * 距离相关常量（单位：米）
     */
    // 打卡距离限制
    public static final double CHECKIN_DISTANCE_LIMIT = 100.0;
    // 附近博物馆距离限制
    public static final double NEARBY_DISTANCE_LIMIT = 5000.0;
    
    /**
     * 时间相关常量（单位：小时）
     */
    // 打卡间隔限制
    public static final int CHECKIN_INTERVAL_LIMIT = 24;
    // 默认展览时长
    public static final int EXHIBITION_DURATION_DEFAULT = 2;
    
    /**
     * 文件相关常量
     */
    public static final String IMAGE_UPLOAD_PATH = "/uploads/images/";
    public static final String AVATAR_UPLOAD_PATH = "/uploads/avatars/";
    // 5MB
    public static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024L;
    public static final String[] ALLOWED_IMAGE_TYPES = {"jpg", "jpeg", "png", "gif"};
    
    /**
     * 推送消息类型常量
     */
    // 打卡消息
    public static final String MESSAGE_TYPE_CHECKIN = "CHECKIN";
    // 展览消息
    public static final String MESSAGE_TYPE_EXHIBITION = "EXHIBITION";
    // 活动消息
    public static final String MESSAGE_TYPE_ACTIVITY = "ACTIVITY";
    // 系统消息
    public static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";
    // 推广消息
    public static final String MESSAGE_TYPE_PROMOTION = "PROMOTION";
    
    /**
     * 审核状态常量
     */
    // 待审核
    public static final int AUDIT_STATUS_PENDING = 0;
    // 审核通过
    public static final int AUDIT_STATUS_APPROVED = 1;
    // 审核拒绝
    public static final int AUDIT_STATUS_REJECTED = 2;
    
    /**
     * 私有构造函数，防止实例化
     */
    private MuseumConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}