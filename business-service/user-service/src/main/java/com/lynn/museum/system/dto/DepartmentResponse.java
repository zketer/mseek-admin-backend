package com.lynn.museum.system.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 部门响应
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "部门响应")
public class DepartmentResponse {

    @Schema(description = "部门ID")
    private Long id;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "部门编码")
    private String deptCode;

    @Schema(description = "父级部门ID")
    private Long parentId;

    @Schema(description = "祖级列表")
    private String ancestors;

    @Schema(description = "部门负责人")
    private String leader;

    @Schema(description = "负责人手机号")
    private String leaderPhone;

    @Schema(description = "负责人邮箱")
    private String leaderEmail;

    @Schema(description = "显示顺序")
    private Integer orderNum;

    @Schema(description = "状态：0-停用，1-正常")
    private Integer status;

    @Schema(description = "部门描述")
    private String description;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateAt;

    @Schema(description = "子部门列表")
    private List<DepartmentResponse> children;

    @Schema(description = "部门用户数量")
    private Long userCount;

}
