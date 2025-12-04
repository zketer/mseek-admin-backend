package com.lynn.museum.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 部门查询请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "部门查询请求")
public class DepartmentQueryRequest {

    @Schema(description = "部门名称（模糊查询）")
    private String deptName;

    @Schema(description = "部门编码（模糊查询）")
    private String deptCode;

    @Schema(description = "父级部门ID")
    private Long parentId;

    @Schema(description = "状态：0-停用，1-正常")
    private Integer status;

    @Schema(description = "部门负责人（模糊查询）")
    private String leader;

}
