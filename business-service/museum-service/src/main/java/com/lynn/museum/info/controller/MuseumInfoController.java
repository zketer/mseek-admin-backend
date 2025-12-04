package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.MuseumCreateRequest;
import com.lynn.museum.info.dto.MuseumQueryRequest;
import com.lynn.museum.info.dto.MuseumResponse;
import com.lynn.museum.info.dto.MuseumUpdateRequest;
import com.lynn.museum.info.service.MuseumInfoService;
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
 * 博物馆信息控制器
 *
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "MuseumInfoController", description = "博物馆信息管理接口")
@RestController
@RequestMapping("info")
@RequiredArgsConstructor
@Validated
public class MuseumInfoController {

    private final MuseumInfoService museumInfoService;

    @Operation(summary = "分页查询博物馆列表")
    // @PreAuthorize("hasAuthority('museum:info:query')")
    @GetMapping
    public Result<IPage<MuseumResponse>> getMuseumPage(@Valid MuseumQueryRequest query) {
        IPage<MuseumResponse> page = museumInfoService.getMuseumPage(query);
        return Result.success(page);
    }

    @Operation(summary = "获取博物馆详情")
    // @PreAuthorize("hasAuthority('museum:info:query')")
    @GetMapping("/{id}")
    public Result<MuseumResponse> getMuseumById(
            @Parameter(description = "博物馆ID") @PathVariable @NotNull Long id) {
        MuseumResponse museum = museumInfoService.getMuseumById(id);
        return Result.success(museum);
    }

    @Operation(summary = "创建博物馆")
    // @PreAuthorize("hasAuthority('museum:info:create')")
    @PostMapping
    public Result<Map<String, Long>> createMuseum(@Valid @RequestBody MuseumCreateRequest request) {
        Long id = museumInfoService.createMuseum(request);
        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return Result.success(result);
    }

    @Operation(summary = "更新博物馆")
    // @PreAuthorize("hasAuthority('museum:info:update')")
    @PutMapping("/{id}")
    public Result<Void> updateMuseum(
            @Parameter(description = "博物馆ID") @PathVariable @NotNull Long id,
            @Valid @RequestBody MuseumUpdateRequest request) {
        request.setId(id);
        museumInfoService.updateMuseum(request);
        return Result.success();
    }

    @Operation(summary = "删除博物馆")
    // @PreAuthorize("hasAuthority('museum:info:delete')")
    @DeleteMapping("/{id}")
    public Result<Void> deleteMuseum(
            @Parameter(description = "博物馆ID") @PathVariable @NotNull Long id) {
        museumInfoService.deleteMuseum(id);
        return Result.success();
    }

    @Operation(summary = "更新博物馆状态")
    // @PreAuthorize("hasAuthority('museum:info:update')")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(
            @Parameter(description = "博物馆ID") @PathVariable @NotNull Long id,
            @Parameter(description = "状态：0-关闭，1-开放") @RequestParam @NotNull Integer status) {
        museumInfoService.updateStatus(id, status);
        return Result.success();
    }

    @Operation(summary = "检查博物馆编码是否存在")
    // @PreAuthorize("hasAuthority('museum:info:check')")
    @GetMapping("/check-code")
    public Result<Boolean> checkCode(
            @Parameter(description = "博物馆编码") @RequestParam String code,
            @Parameter(description = "排除的博物馆ID（可选）") @RequestParam(required = false) Long excludeId) {
        boolean exists = museumInfoService.existsByCode(code, excludeId);
        return Result.success(exists);
    }
}
