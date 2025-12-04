package com.lynn.museum.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 邮件配置类
 * 
 * @author lynn
 * @since 2025-01-01
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "spring.mail")
public class EmailConfig {

    /**
     * SMTP服务器地址
     */
    private String host;

    /**
     * SMTP服务器端口
     */
    private Integer port;

    /**
     * 发件人邮箱
     */
    private String username;

    /**
     * 发件人授权码（163邮箱需要在邮箱设置中开启SMTP服务并生成授权码，不是邮箱登录密码）
     * 获取方式：登录163邮箱 -> 设置 -> POP3/SMTP/IMAP -> 开启服务 -> 获取授权码
     */
    private String password;

    /**
     * 发件人昵称
     */
    private String from;

    /**
     * 验证码有效期（分钟）
     */
    private Integer codeExpireMinutes = 5;
}

