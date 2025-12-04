package com.lynn.museum.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 登录请求DTO
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "登录请求")
public class LoginRequest {

    @Schema(description = "用户名", example = "admin")
    @NotBlank(message = "用户名不能为空")
    private String username;

    @Schema(description = "密码", example = "123456")
    @NotBlank(message = "密码不能为空")
    private String password;

    @Schema(description = "验证码", example = "1234")
    private String captcha;

    @Schema(description = "验证码key", example = "captcha_key_123")
    private String captchaKey;

    @Schema(description = "记住我", example = "false")
    private Boolean rememberMe = false;

    // ==================== 设备信息（可选，APP端使用）====================
    
    @Schema(description = "设备唯一标识（APP端必填，Web端可选）", example = "device_12345")
    private String deviceId;
    
    @Schema(description = "设备名称", example = "iPhone 14 Pro")
    private String deviceName;
    
    @Schema(description = "设备型号", example = "iPhone15,2")
    private String deviceModel;
    
    @Schema(description = "操作系统版本", example = "iOS 17.0")
    private String osVersion;
    
    @Schema(description = "APP版本", example = "1.0.0")
    private String appVersion;
    
    @Schema(description = "平台类型", example = "ios", allowableValues = {"web", "ios", "android"})
    private String platform;

}