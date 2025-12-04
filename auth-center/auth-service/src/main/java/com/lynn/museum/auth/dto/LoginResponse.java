package com.lynn.museum.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

/**
 * 登录响应DTO
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "登录响应")
public class LoginResponse {

    @Schema(description = "访问令牌")
    private String accessToken;

    @Schema(description = "刷新令牌")
    private String refreshToken;

    @Schema(description = "令牌类型", example = "Bearer")
    @Builder.Default
    private String tokenType = "Bearer";

    @Schema(description = "过期时间（秒）", example = "7200")
    private Long expiresIn;

    @Schema(description = "用户信息")
    private UserInfo userInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户信息")
    public static class UserInfo {
        
        @Schema(description = "用户ID")
        private Long userId;
        
        @Schema(description = "用户名")
        private String username;
        
        @Schema(description = "昵称")
        private String nickname;
        
        @Schema(description = "邮箱")
        private String email;
        
        @Schema(description = "手机号")
        private String phone;
        
        @Schema(description = "头像")
        private String avatar;
        
        @Schema(description = "性别：0-未知，1-男，2-女")
        private Integer gender;
        
        @Schema(description = "状态：0-禁用，1-启用")
        private Integer status;
        
        @Schema(description = "最后登录时间")
        private Date lastLoginTime;
        
        @Schema(description = "角色列表")
        private List<String> roles;
        
        @Schema(description = "权限列表")
        private List<String> permissions;
    }

}