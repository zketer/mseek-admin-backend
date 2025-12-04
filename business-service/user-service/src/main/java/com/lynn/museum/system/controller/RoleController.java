package com.lynn.museum.system.controller;

import com.lynn.museum.common.entity.PageResult;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.system.dto.RoleCreateRequest;
import com.lynn.museum.system.dto.RoleQueryRequest;
import com.lynn.museum.system.dto.RoleResponse;
import com.lynn.museum.system.dto.RoleUpdateRequest;
import com.lynn.museum.system.service.RoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 角色管理控制器
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "RoleController", description = "角色管理相关接口")
@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@Validated
public class RoleController {

    private final RoleService roleService;

    @Operation(summary = "根据ID查询角色")
    // // @PreAuthorize("hasAuthority('system:role:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/{id}")
    public Result<RoleResponse> getRoleById(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long id) {
        RoleResponse role = roleService.getById(id);
        return Result.success(role);
    }

    @Operation(summary = "根据角色编码查询角色")
    // // @PreAuthorize("hasAuthority('system:role:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/code/{roleCode}")
    public Result<RoleResponse> getRoleByCode(
            @Parameter(description = "角色编码") @PathVariable @NotNull String roleCode) {
        com.lynn.museum.system.model.entity.Role role = roleService.getByRoleCode(roleCode);
        if (role == null) {
            return Result.error("角色不存在");
        }
        // 转换为 RoleResponse
        RoleResponse response = new RoleResponse();
        response.setId(role.getId());
        response.setRoleName(role.getRoleName());
        response.setRoleCode(role.getRoleCode());
        response.setDescription(role.getDescription());
        response.setSortOrder(role.getSortOrder());
        response.setStatus(role.getStatus());
        return Result.success(response);
    }

    @Operation(summary = "分页查询角色列表")
    // // @PreAuthorize("hasAuthority('system:role:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping
    public Result<PageResult<RoleResponse>> getRolePage(@Valid RoleQueryRequest query) {
        PageResult<RoleResponse> result = roleService.getPage(query);
        return Result.success(result);
    }

    @Operation(summary = "查询所有角色")
    // // @PreAuthorize("hasAuthority('system:role:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/all")
    public Result<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getAllRoles();
        return Result.success(roles);
    }

    @Operation(summary = "查询启用的角色")
    // // @PreAuthorize("hasAuthority('system:role:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/enabled")
    public Result<List<RoleResponse>> getEnabledRoles() {
        List<RoleResponse> roles = roleService.getEnabledRoles();
        return Result.success(roles);
    }

    @Operation(summary = "创建角色")
    // // @PreAuthorize("hasAuthority('system:role:create') or hasRole('INTERNAL_SERVICE')")
    @PostMapping
    public Result<Long> createRole(@Valid @RequestBody RoleCreateRequest request) {
        Long roleId = roleService.createRole(request);
        return Result.success(roleId);
    }

    @Operation(summary = "更新角色")
    // // @PreAuthorize("hasAuthority('system:role:update') or hasRole('INTERNAL_SERVICE')")
    @PutMapping
    public Result<Void> updateRole(@Valid @RequestBody RoleUpdateRequest request) {
        roleService.updateRole(request);
        return Result.success();
    }

    @Operation(summary = "删除角色")
    // // @PreAuthorize("hasAuthority('system:role:delete') or hasRole('INTERNAL_SERVICE')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteRole(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long id) {
        roleService.deleteRole(id);
        return Result.success();
    }

    @Operation(summary = "批量删除角色")
    // // @PreAuthorize("hasAuthority('system:role:delete') or hasRole('INTERNAL_SERVICE')")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatchRoles(
            @Parameter(description = "角色ID列表") @RequestBody @NotEmpty List<Long> ids) {
        roleService.deleteBatchRoles(ids);
        return Result.success();
    }

    @Operation(summary = "启用/禁用角色")
    // // @PreAuthorize("hasAuthority('system:role:update') or hasRole('INTERNAL_SERVICE')")
    @PutMapping("/{id}/status")
    public Result<Void> updateRoleStatus(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam @NotNull Integer status) {
        roleService.updateRoleStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "分配角色权限")
    // // @PreAuthorize("hasAuthority('system:role:assign-permissions') or hasRole('INTERNAL_SERVICE')")
    @PutMapping("/{id}/permissions")
    public Result<Void> assignPermissions(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long id,
            @Parameter(description = "权限ID列表") @RequestBody @NotEmpty List<Long> permissionIds) {
        roleService.assignPermissions(id, permissionIds);
        return Result.success();
    }

    @Operation(summary = "获取角色权限列表")
    // // @PreAuthorize("hasAuthority('system:role:query-permissions') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/{id}/permissions")
    public Result<List<String>> getRolePermissions(
            @Parameter(description = "角色ID") @PathVariable @NotNull Long id) {
        List<String> permissions = roleService.getRolePermissions(id);
        return Result.success(permissions);
    }

    @Operation(summary = "检查角色编码是否存在")
    // // @PreAuthorize("hasAuthority('system:role:check') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/check-code")
    public Result<Boolean> checkRoleCode(
            @Parameter(description = "角色编码") @RequestParam String roleCode,
            @Parameter(description = "排除的角色ID") @RequestParam(required = false) Long excludeId) {
        boolean exists = roleService.existsByRoleCode(roleCode, excludeId);
        return Result.success(exists);
    }

    @Operation(summary = "根据用户ID查询角色列表")
    // // @PreAuthorize("hasAuthority('system:role:query') or hasRole('INTERNAL_SERVICE')")
    @GetMapping("/user/{userId}")
    public Result<List<RoleResponse>> getRolesByUserId(
            @Parameter(description = "用户ID") @PathVariable @NotNull Long userId) {
        List<RoleResponse> roles = roleService.getRolesByUserId(userId);
        return Result.success(roles);
    }

}