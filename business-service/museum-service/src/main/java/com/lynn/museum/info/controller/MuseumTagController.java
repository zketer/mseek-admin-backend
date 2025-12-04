package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.TagCreateRequest;
import com.lynn.museum.info.dto.TagQueryRequest;
import com.lynn.museum.info.dto.TagResponse;
import com.lynn.museum.info.dto.TagUpdateRequest;
import com.lynn.museum.info.service.MuseumTagService;
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
 * 博物馆标签控制器
 *
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "MuseumTagController", description = "博物馆标签管理接口")
@RestController
@RequestMapping("/tags")  // 博物馆标签管理API
@RequiredArgsConstructor
@Validated
public class MuseumTagController {

    private final MuseumTagService museumTagService;

    @Operation(summary = "分页查询标签列表")
    @GetMapping
    public Result<IPage<TagResponse>> getTagPage(@Valid TagQueryRequest query) {
        IPage<TagResponse> page = museumTagService.getTagPage(query);
        return Result.success(page);
    }

    @Operation(summary = "获取所有标签列表")
    @GetMapping("/all")
    public Result<List<TagResponse>> getAllTags() {
        List<TagResponse> tags = museumTagService.getAllTags();
        return Result.success(tags);
    }

    @Operation(summary = "获取标签详情")
    @GetMapping("/{id}")
    public Result<TagResponse> getTagById(
            @Parameter(description = "标签ID") @PathVariable @NotNull Long id) {
        TagResponse tag = museumTagService.getTagById(id);
        return Result.success(tag);
    }

    @Operation(summary = "创建标签")
    @PostMapping
    public Result<Map<String, Long>> createTag(@Valid @RequestBody TagCreateRequest request) {
        Long id = museumTagService.createTag(request);
        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return Result.success(result);
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<Void> updateTag(
            @Parameter(description = "标签ID") @PathVariable @NotNull Long id,
            @Valid @RequestBody TagUpdateRequest request) {
        request.setId(id);
        museumTagService.updateTag(request);
        return Result.success();
    }

    @Operation(summary = "删除标签")
    @DeleteMapping("/{id}")
    public Result<Void> deleteTag(
            @Parameter(description = "标签ID") @PathVariable @NotNull Long id) {
        museumTagService.deleteTag(id);
        return Result.success();
    }

    @Operation(summary = "检查标签编码是否存在")
    @GetMapping("/check-code")
    public Result<Boolean> checkCode(
            @Parameter(description = "标签编码") @RequestParam String code,
            @Parameter(description = "排除的标签ID（可选）") @RequestParam(required = false) Long excludeId) {
        boolean exists = museumTagService.existsByCode(code, excludeId);
        return Result.success(exists);
    }
}
