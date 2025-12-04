package com.lynn.museum.auth.service;

import com.lynn.museum.auth.dto.LoginResponse;

import java.util.List;
import java.util.Map;

/**
 * OAuth 2.0 服务接口
 * 
 * 提供多种第三方登录支持：
 * 1. 微信登录（网站应用、小程序）
 * 2. 支付宝登录（网站应用、小程序）
 * 3. 其他OAuth 2.0提供商扩展
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface OAuth2Service {
    
    /**
     * 生成OAuth2授权URL
     * 
     * @param provider 第三方提供商 (wechat, alipay, etc.)
     * @param redirectUri 回调地址
     * @return 授权URL
     */
    String generateAuthorizeUrl(String provider, String redirectUri);
    
    /**
     * 处理OAuth2回调
     * 
     * @param provider 第三方提供商
     * @param code 授权码
     * @param state 状态参数
     * @return 登录响应
     */
    LoginResponse handleCallback(String provider, String code, String state);
    
    /**
     * 微信小程序登录
     * 
     * @param code 微信小程序授权码
     * @param userInfo 用户信息JSON
     * @return 登录响应
     */
    LoginResponse wechatMiniProgramLogin(String code, String userInfo);
    
    /**
     * 支付宝小程序登录
     * 
     * @param authCode 支付宝小程序授权码
     * @param userInfo 用户信息JSON
     * @return 登录响应
     */
    LoginResponse alipayMiniProgramLogin(String authCode, String userInfo);
    
    /**
     * 获取支持的OAuth2提供商列表
     * 
     * @return 提供商列表
     */
    List<Map<String, Object>> getSupportedProviders();
    
    /**
     * 绑定第三方账号
     * 
     * @param provider 第三方提供商
     * @param code 授权码
     * @param userId 当前用户ID
     */
    void bindThirdPartyAccount(String provider, String code, Long userId);
    
    /**
     * 解绑第三方账号
     * 
     * @param provider 第三方提供商
     * @param userId 当前用户ID
     */
    void unbindThirdPartyAccount(String provider, Long userId);
    
    /**
     * 获取用户绑定的第三方账号列表
     * 
     * @param userId 用户ID
     * @return 绑定账号列表
     */
    List<Map<String, Object>> getUserBindings(Long userId);
}
