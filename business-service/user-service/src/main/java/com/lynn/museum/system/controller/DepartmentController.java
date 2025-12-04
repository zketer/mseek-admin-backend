package com.lynn.museum.system.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.system.dto.DepartmentCreateRequest;
import com.lynn.museum.system.dto.DepartmentQueryRequest;
import com.lynn.museum.system.dto.DepartmentResponse;
import com.lynn.museum.system.dto.DepartmentUpdateRequest;
import com.lynn.museum.system.service.DepartmentService;
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
 * 部门管理控制器
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "DepartmentController", description = "部门管理相关接口")
@RestController
@RequestMapping("/departments")
@RequiredArgsConstructor
@Validated
public class DepartmentController {

    private final DepartmentService departmentService;

    @Operation(summary = "根据ID查询部门")
    // @PreAuthorize("hasAuthority('system:department:query')")
    @GetMapping("/{id}")
    public Result<DepartmentResponse> getDeptById(
            @Parameter(description = "部门ID") @PathVariable @NotNull Long id) {
        DepartmentResponse dept = departmentService.getById(id);
        return Result.success(dept);
    }

    @Operation(summary = "查询部门树")
    // @PreAuthorize("hasAuthority('system:department:query')")
    @GetMapping("/tree")
    public Result<List<DepartmentResponse>> getDepartmentTree(DepartmentQueryRequest query) {
        List<DepartmentResponse> tree = departmentService.getDepartmentTree(query);
        return Result.success(tree);
    }

    @Operation(summary = "查询部门列表")
    // @PreAuthorize("hasAuthority('system:department:query')")
    @GetMapping
    public Result<List<DepartmentResponse>> getDepartmentList(DepartmentQueryRequest query) {
        List<DepartmentResponse> list = departmentService.getDepartmentList(query);
        return Result.success(list);
    }

    @Operation(summary = "查询启用的部门列表")
    // @PreAuthorize("hasAuthority('system:department:query')")
    @GetMapping("/enabled")
    public Result<List<DepartmentResponse>> getEnabledDepartments() {
        List<DepartmentResponse> list = departmentService.getEnabledDepartments();
        return Result.success(list);
    }

    @Operation(summary = "根据父级ID查询子部门")
    @GetMapping("/children/{parentId}")
    public Result<List<DepartmentResponse>> getChildrenByParentId(
            @Parameter(description = "父级部门ID") @PathVariable @NotNull Long parentId) {
        List<DepartmentResponse> children = departmentService.getChildrenByParentId(parentId);
        return Result.success(children);
    }

    @Operation(summary = "创建部门")
    @PostMapping
    public Result<Long> createDepartment(@Valid @RequestBody DepartmentCreateRequest request) {
        Long deptId = departmentService.createDepartment(request);
        return Result.success(deptId);
    }

    @Operation(summary = "更新部门")
    @PutMapping
    public Result<Void> updateDepartment(@Valid @RequestBody DepartmentUpdateRequest request) {
        departmentService.updateDepartment(request);
        return Result.success();
    }

    @Operation(summary = "删除部门")
    @DeleteMapping("/{id}")
    public Result<Void> deleteDepartment(
            @Parameter(description = "部门ID") @PathVariable @NotNull Long id) {
        departmentService.deleteDepartment(id);
        return Result.success();
    }

    @Operation(summary = "批量删除部门")
    @DeleteMapping("/batch")
    public Result<Void> deleteBatchDepartments(
            @Parameter(description = "部门ID列表") @RequestBody @NotEmpty List<Long> ids) {
        departmentService.deleteBatchDepartments(ids);
        return Result.success();
    }

    @Operation(summary = "启用/停用部门")
    @PutMapping("/{id}/status")
    public Result<Void> updateDepartmentStatus(
            @Parameter(description = "部门ID") @PathVariable @NotNull Long id,
            @Parameter(description = "状态：0-停用，1-正常") @RequestParam @NotNull Integer status) {
        departmentService.updateDepartmentStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "检查部门编码是否存在")
    @GetMapping("/check-code")
    public Result<Boolean> checkDeptCode(
            @Parameter(description = "部门编码") @RequestParam String deptCode,
            @Parameter(description = "排除的部门ID") @RequestParam(required = false) Long excludeId) {
        boolean exists = departmentService.existsByDeptCode(deptCode, excludeId);
        return Result.success(exists);
    }

    @Operation(summary = "获取部门用户列表")
    @GetMapping("/{id}/users")
    public Result<List<Long>> getDepartmentUsers(
            @Parameter(description = "部门ID") @PathVariable @NotNull Long id) {
        List<Long> userIds = departmentService.getDepartmentUsers(id);
        return Result.success(userIds);
    }

    @Operation(summary = "移动用户到部门")
    @PutMapping("/{id}/users")
    public Result<Void> moveUsersToDepart(
            @Parameter(description = "部门ID") @PathVariable @NotNull Long id,
            @Parameter(description = "用户ID列表") @RequestBody @NotEmpty List<Long> userIds) {
        departmentService.moveUsersToDepart(id, userIds);
        return Result.success();
    }

}
