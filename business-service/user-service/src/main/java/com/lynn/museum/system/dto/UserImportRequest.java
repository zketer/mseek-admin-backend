package com.lynn.museum.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotNull;

/**
 * 用户导入请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户导入请求")
public class UserImportRequest {

    @Schema(description = "是否更新已存在用户")
    @NotNull(message = "是否更新已存在用户不能为空")
    private Boolean updateSupport = false;

    @Schema(description = "默认密码")
    private String defaultPassword = "123456";

    @Schema(description = "默认部门ID")
    private Long defaultDeptId;

    @Schema(description = "默认角色ID")
    private Long defaultRoleId;

}
