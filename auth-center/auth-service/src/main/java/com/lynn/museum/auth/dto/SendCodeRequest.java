package com.lynn.museum.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 发送验证码请求DTO
 * 
 * @author lynn
 * @since 2025-01-01
 */
@Data
@Schema(description = "发送验证码请求")
public class SendCodeRequest {

    @Schema(description = "邮箱", example = "zhangsan@example.com", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "邮箱不能为空")
    @Email(message = "邮箱格式不正确")
    private String email;

    @Schema(description = "验证码类型：register-注册，reset-密码重置", example = "register", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "验证码类型不能为空")
    private String type;
}

