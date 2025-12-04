package com.lynn.museum.auth.service;

/**
 * 邮件服务接口
 * 
 * @author lynn
 * @since 2025-01-01
 */
public interface EmailService {

    /**
     * 发送注册验证码
     * 
     * @param email 收件人邮箱
     * @return 验证码（6位数字）
     */
    String sendRegisterCode(String email);

    /**
     * 验证验证码
     * 
     * @param email 邮箱
     * @param code 验证码
     * @return 是否验证通过
     */
    boolean verifyCode(String email, String code);

    /**
     * 发送密码重置验证码
     * 
     * @param email 收件人邮箱
     * @return 验证码（6位数字）
     */
    String sendPasswordResetCode(String email);
}

