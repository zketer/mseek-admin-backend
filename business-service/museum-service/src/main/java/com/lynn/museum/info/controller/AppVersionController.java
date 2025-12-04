package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.*;
import com.lynn.museum.info.service.AppVersionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 应用版本管理控制器
 *
 * @author lynn
 * @since 2025-10-27
 */
@Slf4j
@RestController
@RequestMapping("/app-versions")
@RequiredArgsConstructor
@Tag(name = "AppVersionController", description = "移动应用版本管理接口")
public class AppVersionController {

    private final AppVersionService appVersionService;

    @GetMapping
    @Operation(summary = "获取版本列表", description = "分页查询应用版本列表")
    public Result<IPage<AppVersionResponse>> getAppVersions(@Valid AppVersionQueryRequest query) {
        log.info("查询版本列表，查询条件: {}", query);
        IPage<AppVersionResponse> result = appVersionService.getAppVersions(query);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取版本详情", description = "根据ID获取应用版本详情")
    public Result<AppVersionResponse> getAppVersionDetail(
            @Parameter(description = "版本ID") @PathVariable Long id) {
        log.info("查询版本详情，版本ID: {}", id);
        AppVersionResponse result = appVersionService.getAppVersionDetail(id);
        return Result.success(result);
    }

    @PostMapping
    // @PreAuthorize("hasAuthority('app:version:create')")
    @Operation(summary = "创建版本", description = "创建新的应用版本")
    public Result<Map<String, Long>> createAppVersion(
            @Valid @RequestBody AppVersionCreateRequest request) {
        log.info("创建版本，请求参数: {}", request);
        Long id = appVersionService.createAppVersion(request);
        Map<String, Long> data = new HashMap<>();
        data.put("id", id);
        return Result.success(data);
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasAuthority('app:version:update')")
    @Operation(summary = "更新版本", description = "更新应用版本信息")
    public Result<Void> updateAppVersion(
            @Parameter(description = "版本ID") @PathVariable Long id,
            @Valid @RequestBody AppVersionUpdateRequest request) {
        log.info("更新版本，版本ID: {}, 请求参数: {}", id, request);
        appVersionService.updateAppVersion(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('app:version:delete')")
    @Operation(summary = "删除版本", description = "删除应用版本")
    public Result<Void> deleteAppVersion(
            @Parameter(description = "版本ID") @PathVariable Long id) {
        log.info("删除版本，版本ID: {}", id);
        appVersionService.deleteAppVersion(id);
        return Result.success();
    }

    @GetMapping("/stats")
    @Operation(summary = "获取统计信息", description = "获取应用版本统计信息")
    public Result<AppVersionStatsResponse> getAppVersionStats() {
        log.info("查询版本统计信息");
        AppVersionStatsResponse result = appVersionService.getAppVersionStats();
        return Result.success(result);
    }

    @PostMapping("/{id}/download")
    @Operation(summary = "更新下载次数", description = "记录下载行为，增加下载计数")
    public Result<Void> updateDownloadCount(
            @Parameter(description = "版本ID") @PathVariable Long id) {
        log.info("更新下载次数，版本ID: {}", id);
        appVersionService.updateDownloadCount(id);
        return Result.success();
    }

    @PutMapping("/{id}/latest")
    // @PreAuthorize("hasAuthority('app:version:update')")
    @Operation(summary = "标记为最新版本", description = "将指定版本标记为最新版本")
    public Result<Void> markAsLatest(
            @Parameter(description = "版本ID") @PathVariable Long id) {
        log.info("标记为最新版本，版本ID: {}", id);
        appVersionService.markAsLatest(id);
        return Result.success();
    }

    @PutMapping("/{id}/publish")
    // @PreAuthorize("hasAuthority('app:version:publish')")
    @Operation(summary = "发布版本", description = "发布应用版本")
    public Result<Void> publishVersion(
            @Parameter(description = "版本ID") @PathVariable Long id) {
        log.info("发布版本，版本ID: {}", id);
        appVersionService.publishVersion(id);
        return Result.success();
    }

    @PutMapping("/{id}/deprecate")
    // @PreAuthorize("hasAuthority('app:version:deprecate')")
    @Operation(summary = "废弃版本", description = "废弃应用版本")
    public Result<Void> deprecateVersion(
            @Parameter(description = "版本ID") @PathVariable Long id) {
        log.info("废弃版本，版本ID: {}", id);
        appVersionService.deprecateVersion(id);
        return Result.success();
    }

    @GetMapping("/{id}/download-url")
    @Operation(summary = "获取下载URL", description = "获取应用版本下载地址")
    public Result<String> getDownloadUrl(
            @Parameter(description = "版本ID") @PathVariable Long id) {
        log.info("获取下载URL，版本ID: {}", id);
        String url = appVersionService.getDownloadUrl(id);
        return Result.success(url);
    }

    @GetMapping("/latest")
    @Operation(summary = "获取最新版本", description = "获取各平台最新发布版本信息")
    public Result<Map<String, AppVersionResponse>> getLatestVersions() {
        log.info("查询最新版本信息");
        Map<String, AppVersionResponse> result = appVersionService.getLatestVersions();
        return Result.success(result);
    }
}

