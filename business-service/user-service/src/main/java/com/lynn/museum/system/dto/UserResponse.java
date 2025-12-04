package com.lynn.museum.system.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户响应
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "用户响应")
public class UserResponse {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "邮箱")
    private String email;

    @Schema(description = "手机")
    private String phone;

    @Schema(description = "头像")
    private String avatar;

    @Schema(description = "性别：0-保密，1-男，2-女")
    private Integer gender;

    @Schema(description = "生日")
    private LocalDate birthday;

    @Schema(description = "状态：0-禁用，1-正常")
    private Integer status;

    @Schema(description = "最后登录时间")
    private Date lastLoginTime;

    @Schema(description = "最后登录IP")
    private String lastLoginIp;

    @Schema(description = "登录次数")
    private Integer loginCount;

    @Schema(description = "备注")
    private String remark;

    @Schema(description = "创建时间")
    private Date createAt;

    @Schema(description = "更新时间")
    private Date updateAt;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "更新人")
    private String updateBy;

    @Schema(description = "权限列表")
    private List<String> permissions;

    @Schema(description = "角色列表")
    private List<RoleInfo> roles;

    /**
     * 角色信息
     */
    @Data
    public static class RoleInfo {

        @Schema(description = "角色ID")
        private Long roleId;

        @Schema(description = "角色名称")
        private String roleName;

        @Schema(description = "角色编码")
        private String roleCode;
    }

}