package com.lynn.museum.system.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 权限响应
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "权限响应")
public class PermissionResponse {

    @Schema(description = "权限ID")
    private Long id;

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "权限类型：1-菜单，2-按钮")
    private Integer permissionType;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "组件路径")
    private String component;

    @Schema(description = "图标")
    private String icon;

    @Schema(description = "是否可见：0-隐藏，1-显示")
    private Integer visible;

    @Schema(description = "子权限列表")
    private List<PermissionResponse> children;

    @Schema(description = "创建时间")
    private Date createAt;

    @Schema(description = "更新时间")
    private Date updateAt;

    @Schema(description = "创建者")
    private String createBy;

    @Schema(description = "更新者")
    private String updateBy;

    @Schema(description = "备注")
    private String remark;

}