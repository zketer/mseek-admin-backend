package com.lynn.museum.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 权限更新请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "权限更新请求")
public class PermissionUpdateRequest {

    @Schema(description = "权限ID")
    @NotNull(message = "权限ID不能为空")
    private Long id;

    @Schema(description = "权限名称")
    @NotBlank(message = "权限名称不能为空")
    @Size(max = 50, message = "权限名称长度不能超过50个字符")
    private String permissionName;

    @Schema(description = "权限编码")
    @NotBlank(message = "权限编码不能为空")
    @Size(max = 100, message = "权限编码长度不能超过100个字符")
    private String permissionCode;

    @Schema(description = "权限类型：1-菜单，2-按钮")
    @NotNull(message = "权限类型不能为空")
    private Integer permissionType;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "路径")
    @Size(max = 200, message = "路径长度不能超过200个字符")
    private String path;

    @Schema(description = "描述")
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "组件路径")
    @Size(max = 200, message = "组件路径长度不能超过200个字符")
    private String component;

    @Schema(description = "图标")
    @Size(max = 100, message = "图标长度不能超过100个字符")
    private String icon;

    @Schema(description = "是否可见：0-隐藏，1-显示")
    private Integer visible;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

}