package com.lynn.museum.system.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 角色响应
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "角色响应")
public class RoleResponse {

    @Schema(description = "角色ID")
    private Long id;

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "权限列表")
    private List<PermissionInfo> permissions;

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

    /**
     * 权限信息
     */
    @Data
    @Schema(description = "权限信息")
    public static class PermissionInfo {

        @Schema(description = "权限ID")
        private Long id;

        @Schema(description = "权限名称")
        private String permissionName;

        @Schema(description = "权限编码")
        private String permissionCode;

        @Schema(description = "权限类型：1-菜单，2-按钮")
        private Integer permissionType;

    }

}