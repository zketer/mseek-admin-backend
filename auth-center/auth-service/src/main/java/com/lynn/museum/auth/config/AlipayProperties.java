package com.lynn.museum.auth.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 支付宝配置属性
 * 
 * @author lynn
 * @since 2025-01-15
 */
@Data
@Component
@ConfigurationProperties(prefix = "museum.auth.alipay")
public class AlipayProperties {
    
    /**
     * 网站应用配置
     */
    private WebConfig web = new WebConfig();
    
    /**
     * 小程序配置
     */
    private MiniProgramConfig miniprogram = new MiniProgramConfig();
    
    @Data
    public static class WebConfig {
        /**
         * 支付宝应用ID
         */
        private String appId;
        
        /**
         * 应用私钥
         */
        private String privateKey;
        
        /**
         * 支付宝公钥
         */
        private String alipayPublicKey;
        
        /**
         * 支付宝网关地址
         */
        private String serverUrl = "https://openapi.alipay.com/gateway.do";
        
        /**
         * 数据格式
         */
        private String format = "json";
        
        /**
         * 字符集
         */
        private String charset = "utf-8";
        
        /**
         * 签名算法
         */
        private String signType = "RSA2";
        
        /**
         * 授权回调地址
         */
        private String redirectUri;
    }
    
    @Data
    public static class MiniProgramConfig {
        /**
         * 支付宝小程序AppID
         */
        private String appId;
        
        /**
         * 应用私钥
         */
        private String privateKey;
    }
}

