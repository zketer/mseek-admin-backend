package com.lynn.museum.auth.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 认证登录日志实体
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("auth_login_log")
public class AuthLoginLog extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;

    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 登录类型：1-密码登录，2-短信登录，3-第三方登录
     */
    private Integer loginType;
    
    /**
     * 登录结果：1-成功，0-失败
     */
    private Integer loginResult;
    
    /**
     * 失败原因
     */
    private String failureReason;
    
    /**
     * 登录IP
     */
    private String loginIp;
    
    /**
     * 登录地理位置
     */
    private String loginLocation;
    
    /**
     * 用户代理
     */
    private String userAgent;
    
    /**
     * 设备类型
     */
    private String deviceType;
}
