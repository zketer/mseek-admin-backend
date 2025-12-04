package com.lynn.museum.system.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.common.result.ResultCode;
import com.lynn.museum.system.dto.UserBasicInfo;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.system.dto.UserCreateRequest;
import com.lynn.museum.system.dto.UserQueryRequest;
import com.lynn.museum.system.dto.UserResponse;
import com.lynn.museum.system.dto.UserUpdateRequest;
import com.lynn.museum.system.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * 用户管理控制器
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "UserController", description = "用户管理相关接口")
@RestController
@RequestMapping("/users")  // context-path已经是/api/v1/users，所以这里为空
@RequiredArgsConstructor
@Validated
public class UserController {

    private final UserService userService;

    @Operation(summary = "根据ID查询用户")
    // // @PreAuthorize("hasAuthority('system:user:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/{id}")
    public Result<UserResponse> getUserById(
            @Parameter(description = "用户ID") @PathVariable("id") @NotNull Long id) {
        UserResponse user = userService.getById(id);
        return Result.success(user);
    }

    @Operation(summary = "根据用户名查询用户")
    // // @PreAuthorize("hasAuthority('system:user:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/username/{username}")
    public Result<UserResponse> getByUsername(
            @Parameter(description = "用户名") @PathVariable @NotNull String username) {
        UserResponse user = userService.getByUsernameResponse(username);
        return Result.success(user);
    }

    @Operation(summary = "根据用户名查询用户基础信息")
    // // @PreAuthorize("hasAuthority('system:user:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/username/{username}/basic")
    public Result<UserBasicInfo> getUserBasicInfoByUsername(
            @Parameter(description = "用户名") @PathVariable @NotNull String username) {
        UserBasicInfo user = userService.getUserBasicInfoByUsername(username);
        return Result.success(user);
    }

    @Operation(summary = "根据邮箱查询用户基础信息")
    // // @PreAuthorize("hasAuthority('system:user:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/email/{email}/basic")
    public Result<UserBasicInfo> getUserBasicInfoByEmail(
            @Parameter(description = "邮箱") @PathVariable @NotNull String email) {
        UserBasicInfo user = userService.getUserBasicInfoByEmail(email);
        return Result.success(user);
    }

    @Operation(summary = "检查用户名是否存在", description = "用于注册场景，不抛异常，返回true表示已存在，false表示不存在")
    @GetMapping("/check-username/{username}")
    public Result<Boolean> checkUsernameExists(
            @Parameter(description = "用户名") @PathVariable @NotNull String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(exists);
    }

    @Operation(summary = "检查邮箱是否存在", description = "用于注册场景，不抛异常，返回true表示已存在，false表示不存在")
    @GetMapping("/check-email/{email}")
    public Result<Boolean> checkEmailExists(
            @Parameter(description = "邮箱") @PathVariable @NotNull String email) {
        boolean exists = userService.checkEmailExists(email);
        return Result.success(exists);
    }

    @Operation(summary = "分页查询用户列表")
    // // @PreAuthorize("hasAuthority('system:user:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping
    public Result<IPage<UserResponse>> getUserPage(@Valid UserQueryRequest query) {
        IPage<UserResponse> page = userService.getPage(query);
        return Result.success(page);
    }

    @Operation(summary = "创建用户（注册）", description = "用于用户注册，返回用户基础信息")
    // // @PreAuthorize("hasAuthority('system:user:create') or hasRole('INTERNAL_SERVICE')")
    @PostMapping
    public Result<UserBasicInfo> createUser(@RequestBody Map<String, Object> userInfo) {
        UserBasicInfo user = userService.createUserWithBasicInfo(userInfo);
        return Result.success(user);
    }

    @Operation(summary = "创建用户（管理端）", description = "管理端创建用户，返回用户ID")
    // // @PreAuthorize("hasAuthority('system:user:create') or hasRole('INTERNAL_SERVICE')")
    @PostMapping("/admin")
    public Result<Long> createUserForAdmin(@Valid @RequestBody UserCreateRequest request) {
        Long userId = userService.createUser(request);
        return Result.success(userId);
    }

    @Operation(summary = "创建第三方登录用户", description = "为微信、支付宝等第三方登录创建用户")
    // // @PreAuthorize("hasAuthority('system:user:create') or hasRole('INTERNAL_SERVICE')")
    @PostMapping("/third-party")
    public Result<UserBasicInfo> createThirdPartyUser(@RequestBody Map<String, Object> userInfo) {
        UserBasicInfo user = userService.createThirdPartyUser(userInfo);
        return Result.success(user);
    }

    @Operation(summary = "更新用户")
    // // @PreAuthorize("hasAuthority('system:user:update') or hasRole('INTERNAL_SERVICE')")
    @PutMapping
    public Result<Void> updateUser(@Valid @RequestBody UserUpdateRequest request) {
        userService.updateUser(request);
        return Result.success();
    }

    @Operation(summary = "删除用户")
    // // @PreAuthorize("hasAuthority('system:user:delete') or hasRole('INTERNAL_SERVICE')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long id) {
        userService.deleteUser(id);
        return Result.success();
    }

    @Operation(summary = "批量删除用户")
    // // @PreAuthorize("hasAuthority('system:user:delete') or hasRole('INTERNAL_SERVICE')")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatchUsers(
            @Parameter(description = "用户ID列表") @RequestBody @NotEmpty List<Long> ids) {
        userService.deleteBatchUsers(ids);
        return Result.success();
    }

    @Operation(summary = "启用/禁用用户")
    // // @PreAuthorize("hasAuthority('system:user:update') or hasRole('INTERNAL_SERVICE')")
    @PutMapping("/{id}/status")
    public Result<Void> updateUserStatus(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam @NotNull Integer status) {
        userService.updateUserStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "重置用户密码")
    // // @PreAuthorize("hasAuthority('system:user:reset-password') or hasRole('INTERNAL_SERVICE')")
    @PutMapping("/{id}/password")
    public Result<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long id,
            @Parameter(description = "包含新密码的请求体，格式: {\"newPassword\": \"xxx\"}") @RequestBody @NotNull Map<String, String> passwordMap) {
        String newPassword = passwordMap.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BizException(ResultCode.PARAM_ERROR);
        }
        userService.resetPassword(id, newPassword);
        return Result.success();
    }

    @Operation(summary = "分配用户角色")
    // // @PreAuthorize("hasAuthority('system:user:assign-roles') or hasRole('INTERNAL_SERVICE')")
    @PutMapping("/{id}/roles")
    public Result<Void> assignRoles(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long id,
            @Parameter(description = "角色ID列表") @RequestBody @NotEmpty List<Long> roleIds) {
        userService.assignRoles(id, roleIds);
        return Result.success();
    }

    @Operation(summary = "获取用户角色列表")
    // // @PreAuthorize("hasAuthority('system:user:query-roles') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/{id}/roles")
    public Result<List<String>> getUserRoles(
            @Parameter(description = "用户ID") @PathVariable("id") @NotNull Long id) {
        List<String> roles = userService.getUserRoles(id);
        return Result.success(roles);
    }

    @Operation(summary = "获取用户权限列表")
    // // @PreAuthorize("hasAuthority('system:user:query-permissions') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/{id}/permissions")
    public Result<List<String>> getUserPermissions(
            @Parameter(description = "用户ID") @PathVariable("id") @NotNull Long id) {
        List<String> permissions = userService.getUserPermissions(id);
        return Result.success(permissions);
    }

    @Operation(summary = "检查用户名是否存在")
    // // @PreAuthorize("hasAuthority('system:user:check') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(
            @Parameter(description = "用户名") @RequestParam String username,
            @Parameter(description = "排除的用户ID（可选）") @RequestParam(required = false) Long excludeId) {
        boolean exists = userService.existsByUsername(username, excludeId);
        return Result.success(exists);
    }

    @Operation(summary = "检查邮箱是否存在")
    // // @PreAuthorize("hasAuthority('system:user:check') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/check-email")
    public Result<Boolean> checkEmail(
            @Parameter(description = "邮箱") @RequestParam String email,
            @Parameter(description = "排除的用户ID（可选）") @RequestParam(required = false) Long excludeId) {
        boolean exists = userService.existsByEmail(email, excludeId);
        return Result.success(exists);
    }

    @Operation(summary = "检查手机号是否存在")
    // // @PreAuthorize("hasAuthority('system:user:check') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhone(
            @Parameter(description = "手机号") @RequestParam String phone,
            @Parameter(description = "排除的用户ID（可选）") @RequestParam(required = false) Long excludeId) {
        boolean exists = userService.existsByPhone(phone, excludeId);
        return Result.success(exists);
    }

    @Operation(summary = "获取用户统计信息")
    // // @PreAuthorize("hasAuthority('system:user:statistics') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/statistics")
    public Result<Object> getUserStatistics() {
        Object statistics = userService.getUserStatistics();
        return Result.success(statistics);
    }

    @Operation(summary = "根据部门ID查询用户列表")
    @GetMapping("/department/{deptId}")
    public Result<List<UserResponse>> getUsersByDeptId(
            @Parameter(description = "部门ID") @PathVariable @NotNull Long deptId) {
        List<UserResponse> users = userService.getUsersByDeptId(deptId);
        return Result.success(users);
    }

    @Operation(summary = "根据角色ID查询用户列表")
    @GetMapping("/role/{roleId}")
    public Result<List<UserResponse>> getUsersByRoleId(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long roleId) {
        List<UserResponse> users = userService.getUsersByRoleId(roleId);
        return Result.success(users);
    }

    @Operation(summary = "修改用户密码")
    @PutMapping("/{id}/change-password")
    public Result<Void> changePassword(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long id,
            @Parameter(description = "旧密码") @RequestParam @NotNull String oldPassword,
            @Parameter(description = "新密码") @RequestParam @NotNull String newPassword) {
        userService.changePassword(id, oldPassword, newPassword);
        return Result.success();
    }

    /*
     * 用户头像上传（文件上传接口）- 已废弃
     * 统一使用 uploadAvatarBase64 接口
     *
     * @deprecated 使用 uploadAvatarBase64 接口替代
     */
    /*
    @Operation(summary = "用户头像上传")
    @PostMapping("/{id}/avatar")
    public Result<String> uploadAvatar(
            @Parameter(description = "用户ID") @PathVariable String id,
            @Parameter(description = "头像文件") @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        // 处理临时ID，以temp-开头的ID表示临时上传
        if (id.startsWith("temp-")) {
            // 临时上传，直接返回Base64数据
            try {
                String base64Avatar = userService.convertToBase64(file);
                return Result.success(base64Avatar);
            } catch (Exception e) {
                throw new BizException(ResultCode.FAILED_TO_UPLOAD_AVATAR);
            }
        } else {
            // 正常上传，更新用户头像
            try {
                Long userId = Long.parseLong(id);
                String avatarUrl = userService.uploadAvatar(userId, file);
                return Result.success(avatarUrl);
            } catch (NumberFormatException e) {
                throw new BizException(ResultCode.INVALID_USER_ID);
            }
        }
    }
    */
    
    @Operation(summary = "用户头像上传（Base64）")
    @PostMapping("/{id}/avatar/base64")
    public Result<String> uploadAvatarBase64(
            @Parameter(description = "用户ID") @PathVariable String id,
            @RequestBody Map<String, String> data) {
        String base64Avatar = data.get("avatar");
        if (base64Avatar == null || base64Avatar.isEmpty()) {
            throw new BizException(ResultCode.INVALID_BASE64_AVATAR);
        }
        
        // 处理临时ID，以temp-开头的ID表示临时上传
        if (id.startsWith("temp-")) {
            // 临时上传，直接返回Base64数据
            return Result.success(base64Avatar);
        } else {
            // 正常上传，更新用户头像
            try {
                Long userId = Long.parseLong(id);
                return userService.updateUserAvatar(userId, base64Avatar);
            } catch (NumberFormatException e) {
                throw new BizException(ResultCode.INVALID_USER_ID);
            }
        }
    }

    @Operation(summary = "获取用户个人资料")
    @GetMapping("/{id}/profile")
    public Result<Object> getUserProfile(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long id) {
        Object profile = userService.getUserProfile(id);
        return Result.success(profile);
    }

    @Operation(summary = "更新用户个人资料")
    @PutMapping("/{id}/profile")
    public Result<Void> updateUserProfile(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long id,
            @RequestBody @NotNull Map<String, Object> profileData) {
        userService.updateUserProfile(id, profileData);
        return Result.success();
    }

    @Operation(summary = "锁定用户")
    @PutMapping("/{id}/lock")
    public Result<Void> lockUser(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long id,
            @Parameter(description = "锁定原因") @RequestParam(required = false) String reason) {
        userService.lockUser(id, reason);
        return Result.success();
    }

    @Operation(summary = "解锁用户")
    @PutMapping("/{id}/unlock")
    public Result<Void> unlockUser(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long id) {
        userService.unlockUser(id);
        return Result.success();
    }

    @Operation(summary = "批量导出用户")
    @GetMapping("/export")
    public void exportUsers(
            @Parameter(description = "查询条件") UserQueryRequest query,
            jakarta.servlet.http.HttpServletResponse response) {
        try {
            userService.exportUsers(query, response);
        } catch (Exception e) {
            throw new BizException(ResultCode.EXPORT_FAILED);
        }
    }

    @Operation(summary = "批量导入用户")
    @PostMapping("/import")
    public Result<Map<String, Object>> importUsers(
            @Parameter(description = "Excel文件") @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            Map<String, Object> result = userService.importUsers(file);
            return Result.success(result);
        } catch (Exception e) {
            throw new BizException(ResultCode.IMPORT_FAILED);
        }
    }

    @Operation(summary = "下载用户导入模板")
    @GetMapping("/template")
    public void downloadTemplate(jakarta.servlet.http.HttpServletResponse response) {
        try {
            userService.downloadTemplate(response);
        } catch (Exception e) {
            throw new BizException(ResultCode.DOWNLOAD_TEMPLATE_FAILED);
        }
    }


}