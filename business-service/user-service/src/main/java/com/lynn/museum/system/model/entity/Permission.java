package com.lynn.museum.system.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 权限实体类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_permission")
public class Permission extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    /**
     * 权限名称
     */
    private String permissionName;

    /**
     * 权限编码
     */
    private String permissionCode;

    /**
     * 权限类型：1-菜单，2-按钮，3-接口
     */
    private Integer permissionType;

    /**
     * 父级权限ID
     */
    private Long parentId;

    /**
     * 权限路径
     */
    private String path;

    /**
     * 组件路径
     */
    private String component;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    private Integer status;

    /**
     * 是否可见：0-隐藏，1-显示
     */
    private Integer visible;

    /**
     * 权限描述
     */
    private String description;

}