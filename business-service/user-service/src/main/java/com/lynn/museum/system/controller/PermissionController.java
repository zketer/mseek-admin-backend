package com.lynn.museum.system.controller;

import com.lynn.museum.common.entity.PageResult;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.system.dto.PermissionCreateRequest;
import com.lynn.museum.system.dto.PermissionQueryRequest;
import com.lynn.museum.system.dto.PermissionResponse;
import com.lynn.museum.system.dto.PermissionUpdateRequest;
import com.lynn.museum.system.service.PermissionService;
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
 * 权限管理控制器
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "PermissionController", description = "权限管理相关接口")
@RestController
@RequestMapping("/permissions")
@RequiredArgsConstructor
@Validated
public class PermissionController {

    private final PermissionService permissionService;

    @Operation(summary = "根据ID查询权限")
    // // @PreAuthorize("hasAuthority('system:permission:query')")
    @GetMapping("/{id}")
    public Result<PermissionResponse> getPermissionById(
            @Parameter(description = "权限ID", required = true)
            @PathVariable @NotNull Long id) {
        return Result.success(permissionService.getById(id));
    }

    @Operation(summary = "分页查询权限列表")
    // // @PreAuthorize("hasAuthority('system:permission:query')")
    @GetMapping("/page")
    public Result<PageResult<PermissionResponse>> getPermissionPage(@Valid @ModelAttribute PermissionQueryRequest query) {
        return Result.success(permissionService.getPage(query));
    }

    @Operation(summary = "查询所有权限")
    // // @PreAuthorize("hasAuthority('system:permission:query')")
    @GetMapping("/all")
    public Result<List<PermissionResponse>> getAllPermissions() {
        return Result.success(permissionService.getAllPermissions());
    }

    @Operation(summary = "查询启用的权限")
    // // @PreAuthorize("hasAuthority('system:permission:query')")
    @GetMapping("/enabled")
    public Result<List<PermissionResponse>> getEnabledPermissions() {
        return Result.success(permissionService.getEnabledPermissions());
    }

    @Operation(summary = "查询权限树")
    // // @PreAuthorize("hasAuthority('system:permission:query')")
    @GetMapping("/tree")
    public Result<List<PermissionResponse>> getPermissionTree(
            @Parameter(description = "权限类型：1-菜单，2-操作，3-接口，0或不传-全部")
            @RequestParam(required = false) Integer permissionType) {
        return Result.success(permissionService.getPermissionTree(permissionType));
    }

    @Operation(summary = "查询按类型分组的权限树")
    // // @PreAuthorize("hasAuthority('system:permission:query')")
    @GetMapping("/tree-by-type")
    public Result<List<PermissionResponse>> getPermissionTreeByType() {
        return Result.success(permissionService.getPermissionTreeByType());
    }

    @Operation(summary = "根据父级ID查询子权限")
    // // @PreAuthorize("hasAuthority('system:permission:query')")
    @GetMapping("/children/{parentId}")
    public Result<List<PermissionResponse>> getPermissionsByParentId(
            @Parameter(description = "父级权限ID", required = true)
            @PathVariable @NotNull Long parentId,
            @Parameter(description = "权限类型：1-菜单，2-操作，3-接口，0或不传-全部")
            @RequestParam(required = false) Integer permissionType) {
        return Result.success(permissionService.getPermissionsByParentId(parentId, permissionType));
    }

    @Operation(summary = "创建权限")
    // // @PreAuthorize("hasAuthority('system:permission:create')")
    @PostMapping
    public Result<Long> createPermission(@Valid @RequestBody PermissionCreateRequest request) {
        return Result.success(permissionService.createPermission(request));
    }

    @Operation(summary = "更新权限")
    // // @PreAuthorize("hasAuthority('system:permission:update')")
    @PutMapping
    public Result<Void> updatePermission(@Valid @RequestBody PermissionUpdateRequest request) {
        permissionService.updatePermission(request);
        return Result.success();
    }

    @Operation(summary = "删除权限")
    // // @PreAuthorize("hasAuthority('system:permission:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> deletePermission(
            @Parameter(description = "权限ID", required = true)
            @PathVariable @NotNull Long id) {
        permissionService.deletePermission(id);
        return Result.success();
    }

    @Operation(summary = "批量删除权限")
    // // @PreAuthorize("hasAuthority('system:permission:delete')")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatchPermissions(
            @Parameter(description = "权限ID列表", required = true)
            @RequestBody @NotEmpty List<Long> ids) {
        permissionService.deleteBatchPermissions(ids);
        return Result.success();
    }

    @Operation(summary = "启用/禁用权限")
    // // @PreAuthorize("hasAuthority('system:permission:update')")
    @PutMapping("/{id}/status")
    public Result<Void> updatePermissionStatus(
            @Parameter(description = "权限ID", required = true)
            @PathVariable @NotNull Long id,
            @Parameter(description = "状态：1-启用，0-禁用", required = true)
            @RequestParam @NotNull Integer status) {
        permissionService.updatePermissionStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "检查权限编码是否存在")
    // // @PreAuthorize("hasAuthority('system:permission:check')")
    @GetMapping("/exists")
    public Result<Boolean> existsByPermissionCode(
            @Parameter(description = "权限编码", required = true)
            @RequestParam String permissionCode,
            @Parameter(description = "排除的权限ID")
            @RequestParam(required = false) Long excludeId) {
        return Result.success(permissionService.existsByPermissionCode(permissionCode, excludeId));
    }

    @Operation(summary = "根据用户ID查询权限列表")
    @GetMapping("/user/{userId}")
    public Result<List<PermissionResponse>> getPermissionsByUserId(
            @Parameter(description = "用户ID", required = true)
            @PathVariable @NotNull Long userId) {
        return Result.success(permissionService.getPermissionsByUserId(userId));
    }

    @Operation(summary = "根据角色ID查询权限列表")
    @GetMapping("/role/{roleId}")
    public Result<List<PermissionResponse>> getPermissionsByRoleId(
            @Parameter(description = "角色ID", required = true)
            @PathVariable @NotNull Long roleId) {
        return Result.success(permissionService.getPermissionsByRoleId(roleId));
    }

    @Operation(summary = "初始化演示权限数据（仅当无数据时）")
    @PostMapping("/init-demo")
    public Result<Integer> initDemoData() {
        return Result.success(permissionService.initDemoData());
    }

}