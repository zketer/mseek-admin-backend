package com.lynn.museum.api.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 用户登录信息DTO
 * 从认证服务获取的登录相关信息
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginInfo {
    
    /**
     * 最后登录时间
     */
    private Date lastLoginTime;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
    /**
     * 登录次数
     */
    private Integer loginCount;
    
    /**
     * 登录地理位置
     */
    private String loginLocation;
    
    /**
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 用户代理
     */
    private String userAgent;
}
