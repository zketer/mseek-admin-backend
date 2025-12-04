package com.lynn.museum.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 部门更新请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "部门更新请求")
public class DepartmentUpdateRequest {

    @Schema(description = "部门ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "部门ID不能为空")
    private Long id;

    @Schema(description = "部门名称", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "部门名称不能为空")
    @Size(max = 50, message = "部门名称长度不能超过50个字符")
    private String deptName;

    @Schema(description = "部门编码", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "部门编码不能为空")
    @Size(max = 50, message = "部门编码长度不能超过50个字符")
    private String deptCode;

    @Schema(description = "父级部门ID", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "父级部门ID不能为空")
    private Long parentId;

    @Schema(description = "部门负责人")
    @Size(max = 50, message = "部门负责人长度不能超过50个字符")
    private String leader;

    @Schema(description = "负责人手机号")
    @Size(max = 20, message = "负责人手机号长度不能超过20个字符")
    private String leaderPhone;

    @Schema(description = "负责人邮箱")
    @Size(max = 100, message = "负责人邮箱长度不能超过100个字符")
    private String leaderEmail;

    @Schema(description = "显示顺序")
    private Integer orderNum;

    @Schema(description = "状态：0-停用，1-正常")
    private Integer status;

    @Schema(description = "部门描述")
    @Size(max = 200, message = "部门描述长度不能超过200个字符")
    private String description;

    @Schema(description = "备注")
    @Size(max = 500, message = "备注长度不能超过500个字符")
    private String remark;

}
