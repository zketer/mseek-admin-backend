package com.lynn.museum.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 用户导出请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户导出请求")
public class UserExportRequest {

    @Schema(description = "用户ID列表（为空时导出所有）")
    private List<Long> userIds;

    @Schema(description = "部门ID列表")
    private List<Long> deptIds;

    @Schema(description = "角色ID列表")
    private List<Long> roleIds;

    @Schema(description = "用户状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "用户名（模糊查询）")
    private String username;

    @Schema(description = "昵称（模糊查询）")
    private String nickname;

    @Schema(description = "邮箱（模糊查询）")
    private String email;

    @Schema(description = "手机号（模糊查询）")
    private String phone;

    @Schema(description = "导出字段列表")
    private List<String> exportFields;

}
