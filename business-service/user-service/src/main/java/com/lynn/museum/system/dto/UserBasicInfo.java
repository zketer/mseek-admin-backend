package com.lynn.museum.system.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 用户基本信息DTO
 * 用于认证服务调用
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
public class UserBasicInfo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 密码
     */
    private String password;
    
    /**
     * 邮箱
     */
    private String email;
    
    /**
     * 手机号
     */
    private String phone;
    
    /**
     * 昵称
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;
    
    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
    
    /**
     * 创建时间
     */
    private Date createAt;
    
    /**
     * 更新时间
     */
    private Date updateAt;
}
