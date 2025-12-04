package com.lynn.museum.common.constants;

/**
 * 消息常量类
 * 
 * @author lynn
 * @since 1.0.0
 */
public class MessageConstants {

    /**
     * 消息类型常量
     */
    // 系统消息
    public static final String MESSAGE_TYPE_SYSTEM = "SYSTEM";
    // 打卡消息
    public static final String MESSAGE_TYPE_CHECKIN = "CHECKIN";
    // 展览消息
    public static final String MESSAGE_TYPE_EXHIBITION = "EXHIBITION";
    // 活动消息
    public static final String MESSAGE_TYPE_ACTIVITY = "ACTIVITY";
    // 积分消息
    public static final String MESSAGE_TYPE_POINTS = "POINTS";
    // 推广消息
    public static final String MESSAGE_TYPE_PROMOTION = "PROMOTION";
    // 提醒消息
    public static final String MESSAGE_TYPE_REMINDER = "REMINDER";
    
    /**
     * 消息状态常量
     */
    // 未读
    public static final int MESSAGE_STATUS_UNREAD = 0;
    // 已读
    public static final int MESSAGE_STATUS_READ = 1;
    // 已删除
    public static final int MESSAGE_STATUS_DELETED = 2;
    
    /**
     * 消息优先级常量
     */
    // 低优先级
    public static final int MESSAGE_PRIORITY_LOW = 1;
    // 普通优先级
    public static final int MESSAGE_PRIORITY_NORMAL = 2;
    // 高优先级
    public static final int MESSAGE_PRIORITY_HIGH = 3;
    // 紧急优先级
    public static final int MESSAGE_PRIORITY_URGENT = 4;
    
    /**
     * 推送渠道常量
     */
    // APP推送
    public static final String PUSH_CHANNEL_APP = "APP";
    // 短信推送
    public static final String PUSH_CHANNEL_SMS = "SMS";
    // 邮件推送
    public static final String PUSH_CHANNEL_EMAIL = "EMAIL";
    // 微信推送
    public static final String PUSH_CHANNEL_WECHAT = "WECHAT";
    
    /**
     * 短信模板常量
     */
    // 注册验证码
    public static final String SMS_TEMPLATE_REGISTER = "SMS_REGISTER";
    // 登录验证码
    public static final String SMS_TEMPLATE_LOGIN = "SMS_LOGIN";
    // 重置密码
    public static final String SMS_TEMPLATE_RESET_PASSWORD = "SMS_RESET_PWD";
    // 打卡成功
    public static final String SMS_TEMPLATE_CHECKIN_SUCCESS = "SMS_CHECKIN";
    // 活动提醒
    public static final String SMS_TEMPLATE_ACTIVITY_REMIND = "SMS_ACTIVITY";
    
    /**
     * 邮件模板常量
     */
    // 注册确认
    public static final String EMAIL_TEMPLATE_REGISTER = "EMAIL_REGISTER";
    // 重置密码
    public static final String EMAIL_TEMPLATE_RESET_PASSWORD = "EMAIL_RESET_PWD";
    // 欢迎邮件
    public static final String EMAIL_TEMPLATE_WELCOME = "EMAIL_WELCOME";
    // 月度报告
    public static final String EMAIL_TEMPLATE_MONTHLY_REPORT = "EMAIL_REPORT";
    
    /**
     * 消息模板常量
     */
    public static final String MSG_TEMPLATE_CHECKIN_SUCCESS = "恭喜您在{museumName}打卡成功，获得{points}积分！";
    public static final String MSG_TEMPLATE_LEVEL_UP = "恭喜您升级为{level}会员，享受更多特权！";
    public static final String MSG_TEMPLATE_ACTIVITY_START = "您关注的活动'{activityName}'即将开始，请及时参与！";
    public static final String MSG_TEMPLATE_EXHIBITION_NEW = "新展览'{exhibitionName}'已上线，快来参观吧！";
    public static final String MSG_TEMPLATE_POINTS_EXPIRE = "您有{points}积分即将过期，请及时使用！";
    
    /**
     * 系统通知模板常量
     */
    public static final String NOTICE_SYSTEM_MAINTENANCE = "系统将于{time}进行维护，预计耗时{duration}，请合理安排时间。";
    public static final String NOTICE_VERSION_UPDATE = "系统已更新至{version}版本，新增{features}功能。";
    public static final String NOTICE_POLICY_CHANGE = "用户协议已更新，请及时查看最新条款。";
    
    /**
     * 验证码消息模板
     */
    public static final String CAPTCHA_SMS_TEMPLATE = "您的验证码是：{code}，{minutes}分钟内有效，请勿泄露。";
    public static final String CAPTCHA_EMAIL_TEMPLATE = "您的验证码是：{code}，{minutes}分钟内有效，如非本人操作请忽略。";
    
    /**
     * 消息发送状态常量
     */
    // 待发送
    public static final int SEND_STATUS_PENDING = 0;
    // 发送成功
    public static final int SEND_STATUS_SUCCESS = 1;
    // 发送失败
    public static final int SEND_STATUS_FAILED = 2;
    // 重试中
    public static final int SEND_STATUS_RETRY = 3;
    
    /**
     * 消息队列主题常量
     */
    // 消息主题
    public static final String MQ_TOPIC_MESSAGE = "museum.message";
    // 短信主题
    public static final String MQ_TOPIC_SMS = "museum.sms";
    // 邮件主题
    public static final String MQ_TOPIC_EMAIL = "museum.email";
    // 推送主题
    public static final String MQ_TOPIC_PUSH = "museum.push";
    
    /**
     * 消息队列标签常量
     */
    // 系统消息标签
    public static final String MQ_TAG_SYSTEM = "system";
    // 业务消息标签
    public static final String MQ_TAG_BUSINESS = "business";
    // 营销消息标签
    public static final String MQ_TAG_MARKETING = "marketing";
    
    /**
     * 消息限制常量
     */
    // 消息最大长度
    public static final int MAX_MESSAGE_LENGTH = 500;
    // 标题最大长度
    public static final int MAX_TITLE_LENGTH = 100;
    // 每日短信发送上限
    public static final int MAX_DAILY_SMS_COUNT = 10;
    // 每日邮件发送上限
    public static final int MAX_DAILY_EMAIL_COUNT = 20;
    
    /**
     * 消息有效期常量（单位：天）
     */
    // 消息保存期限
    public static final int MESSAGE_EXPIRE_DAYS = 30;
    // 系统消息保存期限
    public static final int SYSTEM_MESSAGE_EXPIRE_DAYS = 90;
    // 推广消息保存期限
    public static final int PROMOTION_MESSAGE_EXPIRE_DAYS = 7;
    
    /**
     * 私有构造函数，防止实例化
     */
    private MessageConstants() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }
}