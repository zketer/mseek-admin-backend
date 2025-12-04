package com.lynn.museum.system.dto;

import com.lynn.museum.common.constants.ValidationConstants;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 用户创建请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
public class UserCreateRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名" + ValidationConstants.Common.NOT_BLANK)
    @Size(min = ValidationConstants.User.USERNAME_MIN_LENGTH, 
          max = ValidationConstants.User.USERNAME_MAX_LENGTH, 
          message = "用户名长度必须在" + ValidationConstants.User.USERNAME_MIN_LENGTH + "-" + ValidationConstants.User.USERNAME_MAX_LENGTH + "个字符之间")
    @Pattern(regexp = ValidationConstants.User.USERNAME_PATTERN, 
             message = ValidationConstants.User.USERNAME_MESSAGE)
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码" + ValidationConstants.Common.NOT_BLANK)
    @Size(min = ValidationConstants.User.PASSWORD_MIN_LENGTH, 
          max = ValidationConstants.User.PASSWORD_MAX_LENGTH, 
          message = ValidationConstants.User.PASSWORD_MESSAGE)
    private String password;

    /**
     * 邮箱
     */
    @Email(message = ValidationConstants.Common.EMAIL_FORMAT)
    private String email;

    /**
     * 手机号
     */
    @Pattern(regexp = ValidationConstants.User.PHONE_PATTERN, 
             message = ValidationConstants.User.PHONE_MESSAGE)
    private String phone;

    /**
     * 昵称
     */
    @Size(max = ValidationConstants.User.NICKNAME_MAX_LENGTH, 
          message = ValidationConstants.User.NICKNAME_MESSAGE)
    private String nickname;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 性别：0-未知，1-男，2-女
     */
    @Min(value = ValidationConstants.User.GENDER_MIN, message = ValidationConstants.User.GENDER_MESSAGE)
    @Max(value = ValidationConstants.User.GENDER_MAX, message = ValidationConstants.User.GENDER_MESSAGE)
    private Integer gender;

    /**
     * 生日
     */
    private LocalDate birthday;

    /**
     * 状态：0-禁用，1-启用
     */
    @NotNull(message = "状态" + ValidationConstants.Common.NOT_NULL)
    @Min(value = ValidationConstants.User.STATUS_MIN, message = ValidationConstants.User.STATUS_MESSAGE)
    @Max(value = ValidationConstants.User.STATUS_MAX, message = ValidationConstants.User.STATUS_MESSAGE)
    private Integer status;

    /**
     * 角色ID列表
     */
    private List<Long> roleIds;

    /**
     * 备注
     */
    @Size(max = ValidationConstants.User.REMARK_MAX_LENGTH, 
          message = ValidationConstants.User.REMARK_MESSAGE)
    private String remark;

}