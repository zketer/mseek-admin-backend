package com.lynn.museum.auth.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户登录信息响应DTO
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户登录信息响应")
public class UserLoginInfoResponse {
    
    @Schema(description = "最后登录时间")
    private Date lastLoginTime;
    
    @Schema(description = "最后登录IP")
    private String lastLoginIp;
    
    @Schema(description = "登录次数")
    private Integer loginCount;
    
    @Schema(description = "登录地理位置")
    private String loginLocation;
    
    @Schema(description = "设备类型")
    private String deviceType;
    
    @Schema(description = "用户代理")
    private String userAgent;
}
