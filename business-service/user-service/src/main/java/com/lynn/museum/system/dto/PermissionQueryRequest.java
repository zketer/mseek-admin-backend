package com.lynn.museum.system.dto;

import java.util.Date;

import com.lynn.museum.common.entity.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 权限查询请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "权限查询请求")
public class PermissionQueryRequest extends PageQuery {

    @Schema(description = "权限名称")
    private String permissionName;

    @Schema(description = "权限编码")
    private String permissionCode;

    @Schema(description = "权限类型：1-菜单，2-按钮")
    private Integer permissionType;

    @Schema(description = "父级ID")
    private Long parentId;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "路径")
    private String path;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间开始")
    private String createAteStart;

    @Schema(description = "创建时间结束")
    private String createAteEnd;

}