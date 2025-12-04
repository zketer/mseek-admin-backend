package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.ExhibitionCreateRequest;
import com.lynn.museum.info.dto.ExhibitionQueryRequest;
import com.lynn.museum.info.dto.ExhibitionResponse;
import com.lynn.museum.info.dto.ExhibitionUpdateRequest;
import com.lynn.museum.info.service.MuseumExhibitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 博物馆展览控制器
 *
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "MuseumExhibitionController", description = "博物馆展览管理接口")
@RestController
@RequestMapping("/{museumId}/exhibitions")  // 特定博物馆的展览API
@RequiredArgsConstructor
@Validated
public class MuseumExhibitionController {

    private final MuseumExhibitionService museumExhibitionService;

    @Operation(summary = "分页查询展览列表")
    // @PreAuthorize("hasAuthority('museum:exhibition:query')")
    @GetMapping
    public Result<IPage<ExhibitionResponse>> getExhibitionPage(
            @Parameter(description = "博物馆ID") @PathVariable @NotNull Long museumId,
            @Valid ExhibitionQueryRequest query) {
        query.setMuseumId(museumId);
        IPage<ExhibitionResponse> page = museumExhibitionService.getExhibitionPage(query);
        return Result.success(page);
    }

    @Operation(summary = "获取展览详情")
    // @PreAuthorize("hasAuthority('museum:exhibition:query')")
    @GetMapping("/{id}")
    public Result<ExhibitionResponse> getExhibitionById(
            @Parameter(description = "博物馆ID") @PathVariable @NotNull Long museumId,
            @Parameter(description = "展览ID") @PathVariable @NotNull Long id) {
        ExhibitionResponse exhibition = museumExhibitionService.getExhibitionById(id);
        if (!exhibition.getMuseumId().equals(museumId)) {
            return Result.error("展览不属于该博物馆");
        }
        return Result.success(exhibition);
    }

    @Operation(summary = "创建展览")
    @PostMapping
    public Result<Map<String, Long>> createExhibition(
            @Parameter(description = "博物馆ID") @PathVariable @NotNull Long museumId,
            @Valid @RequestBody ExhibitionCreateRequest request) {
        request.setMuseumId(museumId);
        Long id = museumExhibitionService.createExhibition(request);
        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return Result.success(result);
    }

    @Operation(summary = "更新展览")
    @PutMapping("/{id}")
    public Result<Void> updateExhibition(
            @Parameter(description = "博物馆ID") @PathVariable @NotNull Long museumId,
            @Parameter(description = "展览ID") @PathVariable @NotNull Long id,
            @Valid @RequestBody ExhibitionUpdateRequest request) {
        request.setId(id);
        request.setMuseumId(museumId);
        museumExhibitionService.updateExhibition(request);
        return Result.success();
    }

    @Operation(summary = "删除展览")
    @DeleteMapping("/{id}")
    public Result<Void> deleteExhibition(
            @Parameter(description = "博物馆ID") @PathVariable @NotNull Long museumId,
            @Parameter(description = "展览ID") @PathVariable @NotNull Long id) {
        // 检查展览是否属于该博物馆
        ExhibitionResponse exhibition = museumExhibitionService.getExhibitionById(id);
        if (!exhibition.getMuseumId().equals(museumId)) {
            return Result.error("展览不属于该博物馆");
        }
        museumExhibitionService.deleteExhibition(id);
        return Result.success();
    }

    @Operation(summary = "更新展览状态")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "博物馆ID") @PathVariable @NotNull Long museumId,
            @Parameter(description = "展览ID") @PathVariable @NotNull Long id,
            @Parameter(description = "状态：0-已结束，1-进行中，2-未开始") @RequestParam @NotNull Integer status) {
        // 检查展览是否属于该博物馆
        ExhibitionResponse exhibition = museumExhibitionService.getExhibitionById(id);
        if (!exhibition.getMuseumId().equals(museumId)) {
            return Result.error("展览不属于该博物馆");
        }
        museumExhibitionService.updateStatus(id, status);
        return Result.success();
    }
}