package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.CategoryCreateRequest;
import com.lynn.museum.info.dto.CategoryQueryRequest;
import com.lynn.museum.info.dto.CategoryResponse;
import com.lynn.museum.info.dto.CategoryUpdateRequest;
import com.lynn.museum.info.service.MuseumCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 博物馆分类控制器
 *
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "MuseumCategoryController", description = "博物馆分类管理接口")
@RestController
@RequestMapping("/categories")  // 博物馆分类管理API
@RequiredArgsConstructor
@Validated
public class MuseumCategoryController {

    private final MuseumCategoryService museumCategoryService;

    @Operation(summary = "分页查询分类列表")
    @GetMapping
    public Result<IPage<CategoryResponse>> getCategoryPage(@Valid CategoryQueryRequest query) {
        IPage<CategoryResponse> page = museumCategoryService.getCategoryPage(query);
        return Result.success(page);
    }

    @Operation(summary = "获取所有分类列表")
    @GetMapping("/all")
    public Result<List<CategoryResponse>> getAllCategories() {
        List<CategoryResponse> categories = museumCategoryService.getAllCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public Result<CategoryResponse> getCategoryById(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long id) {
        CategoryResponse category = museumCategoryService.getCategoryById(id);
        return Result.success(category);
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public Result<Map<String, Long>> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        Long id = museumCategoryService.createCategory(request);
        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return Result.success(result);
    }

    @Operation(summary = "更新分类")
    @PutMapping("/{id}")
    public Result<Void> updateCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long id,
            @Valid @RequestBody CategoryUpdateRequest request) {
        request.setId(id);
        museumCategoryService.updateCategory(request);
        return Result.success();
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public Result<Void> deleteCategory(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long id) {
        museumCategoryService.deleteCategory(id);
        return Result.success();
    }

    @Operation(summary = "更新分类状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "分类ID") @PathVariable @NotNull Long id,
            @Parameter(description = "状态：0-禁用，1-启用") @RequestParam @NotNull Integer status) {
        museumCategoryService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "检查分类编码是否存在")
    @GetMapping("/check-code")
    public Result<Boolean> checkCode(
            @Parameter(description = "分类编码") @RequestParam String code,
            @Parameter(description = "排除的分类ID（可选）") @RequestParam(required = false) Long excludeId) {
        boolean exists = museumCategoryService.existsByCode(code, excludeId);
        return Result.success(exists);
    }
}
