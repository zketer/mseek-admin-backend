package com.lynn.museum.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 微信配置属性
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Component
@ConfigurationProperties(prefix = "museum.auth.wechat")
public class WechatProperties {

    /**
     * 微信小程序配置
     */
    private Miniprogram miniprogram = new Miniprogram();

    /**
     * 微信Web开放平台配置（扫码登录）
     */
    private Web web = new Web();

    @Data
    public static class Miniprogram {
        /**
         * 小程序AppID
         */
        private String appId;

        /**
         * 小程序AppSecret
         */
        private String appSecret;

        /**
         * 微信API基础URL
         */
        private String apiBaseUrl = "https://api.weixin.qq.com";
    }

    @Data
    public static class Web {
        /**
         * 开放平台AppID（网站应用）
         * 测试模式：填写公众平台测试号的appID
         */
        private String appId;

        /**
         * 开放平台AppSecret
         * 测试模式：填写公众平台测试号的appsecret
         */
        private String appSecret;

        /**
         * 授权回调地址
         */
        private String redirectUri;

        /**
         * 微信开放平台API基础URL
         */
        private String apiBaseUrl = "https://api.weixin.qq.com";
        
        /**
         * 是否启用测试模式
         * true: 使用公众号网页授权（适合测试号）
         * false: 使用开放平台扫码登录（需要企业认证）
         */
        private Boolean testMode = false;
    }
}
