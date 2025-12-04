package com.lynn.museum.api.user.dto;

import lombok.Data;

/**
 * 角色基础信息DTO
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
public class RoleBasicInfo {

    /**
     * 角色ID
     */
    private Long id;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 角色编码
     */
    private String roleCode;

    /**
     * 角色描述
     */
    private String description;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;
}

