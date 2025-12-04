package com.lynn.museum.auth.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * OAuth2第三方身份提供商绑定实体
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("oauth2_user_provider")
public class OAuth2UserProvider extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;
    
    // 重写父类字段以映射正确的数据库列名（数据库是 create_at 和 update_at，没有d）
    // 移除 fill 属性，让数据库的 DEFAULT CURRENT_TIMESTAMP 生效
    @TableField(value = "create_at")
    private java.util.Date createAt;
    
    @TableField(value = "update_at")
    private java.util.Date updateAt;

    /**
     * 本系统用户ID（关联user-service的sys_user.id）
     */
    private Long userId;

    /**
     * 身份提供商（wechat、alipay等）
     */
    private String provider;

    /**
     * 第三方用户ID（openid等）
     */
    private String providerUserId;

    /**
     * 第三方用户名
     */
    private String providerUsername;

    /**
     * 第三方邮箱
     */
    private String providerEmail;

    /**
     * 第三方头像
     */
    private String providerAvatar;

    /**
     * 联合ID（unionid等）
     */
    private String unionId;

    /**
     * 第三方原始用户信息
     */
    private String rawUserInfo;

    /**
     * 绑定时间
     */
    private Date bindTime;

    /**
     * 最后登录时间
     */
    private Date lastLoginTime;

    /**
     * 状态（0：已解绑，1：已绑定）
     */
    private Integer status;
}