package com.lynn.museum.auth.controller;

import com.lynn.museum.auth.dto.LoginResponse;
import com.lynn.museum.auth.dto.WechatMiniProgramLoginRequest;
import com.lynn.museum.auth.service.OAuth2Service;
import com.lynn.museum.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.NotBlank;

/**
 * OAuth 2.0 认证控制器
 * 
 * 支持多种第三方登录方式：
 * 1. 微信登录
 * 2. 支付宝登录
 * 3. 标准OAuth 2.0流程
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/oauth2")
@RequiredArgsConstructor
@Tag(name = "OAuth2Controller", description = "OAuth 2.0 第三方登录接口")
public class OAuth2Controller {

    private final OAuth2Service oauth2Service;

    @GetMapping("/authorize/{provider}")
    @Operation(summary = "OAuth2授权", description = "跳转到第三方授权页面")
    public Result<String> authorize(
            @Parameter(description = "第三方登录提供商", example = "wechat") 
            @PathVariable String provider,
            @Parameter(description = "授权成功后的回调地址") 
            @RequestParam(required = false) String redirectUri) {
        
        log.info("OAuth2授权请求: provider={}, redirectUri={}", provider, redirectUri);
        
        String authorizeUrl = oauth2Service.generateAuthorizeUrl(provider, redirectUri);
        return Result.success(authorizeUrl);
    }

    @PostMapping("/callback/{provider}")
    @Operation(summary = "OAuth2回调", description = "处理第三方登录回调")
    public Result<LoginResponse> callback(
            @Parameter(description = "第三方登录提供商") 
            @PathVariable String provider,
            @Parameter(description = "授权码") 
            @RequestParam @NotBlank String code,
            @Parameter(description = "状态参数") 
            @RequestParam(required = false) String state) {
        
        log.info("OAuth2回调处理: provider={}, code={}, state={}", provider, code, state);
        
        LoginResponse response = oauth2Service.handleCallback(provider, code, state);
        return Result.success(response);
    }

    @PostMapping("/wechat/miniprogram")
    @Operation(summary = "微信小程序登录", description = "使用微信小程序code登录")
    public Result<LoginResponse> wechatMiniProgram(@RequestBody WechatMiniProgramLoginRequest request) {
        
        log.info("微信小程序登录请求: {}", request);
        log.info("接收到的code: {}", request.getCode());
        log.info("接收到的userInfo: {}", request.getUserInfo());
        
        if (request.getCode() == null || request.getCode().trim().isEmpty()) {
            log.error("code参数为空");
            return Result.error("缺少必要参数: code");
        }
        
        LoginResponse response = oauth2Service.wechatMiniProgramLogin(request.getCode(), request.getUserInfo());
        return Result.success(response);
    }

    @PostMapping("/alipay/miniprogram")
    @Operation(summary = "支付宝小程序登录", description = "使用支付宝小程序auth_code登录")
    public Result<LoginResponse> alipayMiniProgram(
            @Parameter(description = "支付宝小程序授权码") 
            @RequestParam @NotBlank String authCode,
            @Parameter(description = "用户信息") 
            @RequestParam(required = false) String userInfo) {
        
        log.info("支付宝小程序登录: authCode={}", authCode);
        
        LoginResponse response = oauth2Service.alipayMiniProgramLogin(authCode, userInfo);
        return Result.success(response);
    }

    @GetMapping("/providers")
    @Operation(summary = "获取支持的OAuth2提供商列表")
    public Result<Object> getProviders() {
        return Result.success(oauth2Service.getSupportedProviders());
    }

    @PostMapping("/bind/{provider}")
    @Operation(summary = "绑定第三方账号", description = "将第三方账号绑定到当前用户")
    public Result<Void> bindThirdPartyAccount(
            @Parameter(description = "第三方登录提供商") 
            @PathVariable String provider,
            @Parameter(description = "授权码") 
            @RequestParam @NotBlank String code,
            @Parameter(description = "当前用户ID") 
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("绑定第三方账号: provider={}, userId={}", provider, userId);
        
        oauth2Service.bindThirdPartyAccount(provider, code, userId);
        return Result.success();
    }

    @DeleteMapping("/unbind/{provider}")
    @Operation(summary = "解绑第三方账号", description = "解除第三方账号绑定")
    public Result<Void> unbindThirdPartyAccount(
            @Parameter(description = "第三方登录提供商") 
            @PathVariable String provider,
            @Parameter(description = "当前用户ID") 
            @RequestHeader("X-User-ID") Long userId) {
        
        log.info("解绑第三方账号: provider={}, userId={}", provider, userId);
        
        oauth2Service.unbindThirdPartyAccount(provider, userId);
        return Result.success();
    }

    @GetMapping("/user/bindings")
    @Operation(summary = "获取用户绑定的第三方账号列表")
    public Result<Object> getUserBindings(
            @Parameter(description = "当前用户ID") 
            @RequestHeader("X-User-ID") Long userId) {
        
        return Result.success(oauth2Service.getUserBindings(userId));
    }
}
