package com.lynn.museum.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * GitHub配置属性
 * 
 * @author lynn
 * @since 2025-10-28
 */
@Data
@Component
@ConfigurationProperties(prefix = "museum.auth.github")
public class GithubProperties {
    
    /**
     * 网站应用配置
     */
    private WebConfig web = new WebConfig();
    
    @Data
    public static class WebConfig {
        /**
         * GitHub OAuth应用Client ID
         */
        private String clientId;
        
        /**
         * GitHub OAuth应用Client Secret
         */
        private String clientSecret;
        
        /**
         * GitHub授权地址
         */
        private String authorizeUrl = "https://github.com/login/oauth/authorize";
        
        /**
         * GitHub获取Access Token地址
         */
        private String tokenUrl = "https://github.com/login/oauth/access_token";
        
        /**
         * GitHub获取用户信息地址
         */
        private String userInfoUrl = "https://api.github.com/user";
        
        /**
         * 授权回调地址
         */
        private String redirectUri;
        
        /**
         * 授权范围
         */
        private String scope = "read:user user:email";
    }
}

