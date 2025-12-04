package com.lynn.museum.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * QQ互联配置属性
 * 
 * @author lynn
 * @since 2025-10-28
 */
@Data
@Component
@ConfigurationProperties(prefix = "museum.auth.qq")
public class QqProperties {
    
    /**
     * 网站应用配置
     */
    private WebConfig web = new WebConfig();
    
    @Data
    public static class WebConfig {
        /**
         * QQ互联应用ID (App ID)
         */
        private String appId;
        
        /**
         * QQ互联应用密钥 (App Key)
         */
        private String appKey;
        
        /**
         * QQ互联授权地址
         */
        private String authorizeUrl = "https://graph.qq.com/oauth2.0/authorize";
        
        /**
         * QQ互联获取Access Token地址
         */
        private String tokenUrl = "https://graph.qq.com/oauth2.0/token";
        
        /**
         * QQ互联获取OpenID地址
         */
        private String openIdUrl = "https://graph.qq.com/oauth2.0/me";
        
        /**
         * QQ互联获取用户信息地址
         */
        private String userInfoUrl = "https://graph.qq.com/user/get_user_info";
        
        /**
         * 授权回调地址
         */
        private String redirectUri;
        
        /**
         * 授权范围
         */
        private String scope = "get_user_info";
        
        /**
         * 响应类型
         */
        private String responseType = "code";
        
        /**
         * 显示方式
         */
        private String display = "pc";
    }
}

