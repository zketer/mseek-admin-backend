package com.lynn.museum.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 角色更新请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "角色更新请求")
public class RoleUpdateRequest {

    @Schema(description = "角色ID")
    @NotNull(message = "角色ID不能为空")
    private Long id;

    @Schema(description = "角色名称")
    @NotBlank(message = "角色名称不能为空")
    @Size(max = 50, message = "角色名称长度不能超过50个字符")
    private String roleName;

    @Schema(description = "角色编码")
    @NotBlank(message = "角色编码不能为空")
    @Size(max = 50, message = "角色编码长度不能超过50个字符")
    private String roleCode;

    @Schema(description = "描述")
    @Size(max = 200, message = "描述长度不能超过200个字符")
    private String description;

    @Schema(description = "状态：0-禁用，1-启用")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "权限ID列表")
    private List<Long> permissionIds;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

}