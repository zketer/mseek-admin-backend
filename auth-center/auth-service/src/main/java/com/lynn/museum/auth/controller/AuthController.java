package com.lynn.museum.auth.controller;

import com.lynn.museum.auth.dto.CaptchaResponse;
import com.lynn.museum.auth.dto.LoginRequest;
import com.lynn.museum.auth.dto.LoginResponse;
import com.lynn.museum.auth.dto.RegisterRequest;
import com.lynn.museum.auth.dto.ResetPasswordRequest;
import com.lynn.museum.auth.dto.SendCodeRequest;
import com.lynn.museum.auth.service.AuthService;
import com.lynn.museum.auth.service.CaptchaService;
import com.lynn.museum.auth.service.EmailService;
import com.lynn.museum.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("")  // context-path已经是/api/v1/auth，所以这里为空
@RequiredArgsConstructor
@Tag(name = "AuthController", description = "用户认证相关接口")
public class AuthController {

    private final AuthService authService;
    private final EmailService emailService;
    private final CaptchaService captchaService;

    @GetMapping("/captcha")
    @Operation(summary = "获取图形验证码")
    public Result<CaptchaResponse> getCaptcha() {
        CaptchaResponse captcha = captchaService.generateCaptcha();
        return Result.success(captcha);
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return Result.success(response);
    }

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return Result.success(response);
    }

    @PostMapping("/send-code")
    @Operation(summary = "发送验证码")
    public Result<Void> sendCode(@Valid @RequestBody SendCodeRequest request) {
        String email = request.getEmail();
        String type = request.getType();
        
        log.info("📧 发送验证码: email={}, type={}", email, type);
        
        if ("register".equals(type)) {
            emailService.sendRegisterCode(email);
        } else if ("reset".equals(type)) {
            emailService.sendPasswordResetCode(email);
        } else {
            return Result.error("无效的验证码类型");
        }
        
        return Result.success();
    }

    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌")
    public Result<LoginResponse> refreshToken(@RequestParam String refreshToken) {
        LoginResponse response = authService.refreshToken(refreshToken);
        return Result.success(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout() {
        authService.logout();
        return Result.success();
    }

    @PostMapping("/reset-password")
    @Operation(summary = "重置密码（忘记密码）")
    public Result<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPasswordByEmail(request);
        return Result.success();
    }

    @GetMapping("/health")
    @Operation(summary = "健康检查")
    public Result<String> health() {
        return Result.success("Auth service is running");
    }

    @GetMapping("/info")
    @Operation(summary = "服务信息")
    public Result<String> info() {
        return Result.success("Museum Auth Service v0.0.1");
    }

}