package com.lynn.museum.system.dto;

import java.util.Date;

import com.lynn.museum.common.entity.PageQuery;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

/**
 * 用户查询请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class UserQueryRequest extends PageQuery {

    /**
     * 用户名
     */
    private String username;

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
     * 性别：0-未知，1-男，2-女
     */
    private Integer gender;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 创建时间开始
     */
    private LocalDate createAteStart;

    /**
     * 创建时间结束
     */
    private LocalDate createAteEnd;
    
    
    /**
     * 是否删除：0-未删除，1-已删除
     */
    private Integer deleted;

}