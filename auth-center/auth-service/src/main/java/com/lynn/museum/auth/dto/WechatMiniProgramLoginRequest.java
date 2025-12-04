package com.lynn.museum.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 微信小程序登录请求DTO
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "微信小程序登录请求")
public class WechatMiniProgramLoginRequest {

    @NotBlank(message = "微信小程序授权码不能为空")
    @Schema(description = "微信小程序授权码")
    private String code;

    @Schema(description = "用户信息JSON字符串")
    private String userInfo;
}
