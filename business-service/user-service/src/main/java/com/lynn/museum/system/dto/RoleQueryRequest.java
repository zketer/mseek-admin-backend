package com.lynn.museum.system.dto;

import java.util.Date;

import com.lynn.museum.common.entity.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色查询请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "角色查询请求")
public class RoleQueryRequest extends PageQuery {

    @Schema(description = "角色名称")
    private String roleName;

    @Schema(description = "角色编码")
    private String roleCode;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "描述")
    private String description;

    @Schema(description = "创建时间开始")
    private String createAteStart;

    @Schema(description = "创建时间结束")
    private String createAteEnd;

}