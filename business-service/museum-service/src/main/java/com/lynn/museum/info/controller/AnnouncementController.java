package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.AnnouncementCreateRequest;
import com.lynn.museum.info.dto.AnnouncementQueryRequest;
import com.lynn.museum.info.dto.AnnouncementResponse;
import com.lynn.museum.info.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * 公告管理控制器
 */
@RestController
@RequestMapping("/announcements")
@Tag(name = "AnnouncementController", description = "公告管理相关接口")
@Slf4j
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    // @PreAuthorize("hasAuthority('content:announcement:query')")
    @Operation(summary = "分页查询公告", description = "支持按标题、类型、状态查询公告")
    public Result<IPage<AnnouncementResponse>> getAnnouncements(@Valid AnnouncementQueryRequest request) {
        log.info("获取公告列表，参数：{}", request);
        IPage<AnnouncementResponse> result = announcementService.getAnnouncementList(request);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('content:announcement:query')")
    @Operation(summary = "获取公告详情", description = "根据ID获取公告详情")
    public Result<AnnouncementResponse> getAnnouncementDetail(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        log.info("获取公告详情，ID：{}", id);
        AnnouncementResponse result = announcementService.getAnnouncementDetail(id);
        return Result.success(result);
    }

    @PostMapping
    // @PreAuthorize("hasAuthority('content:announcement:create')")
    @Operation(summary = "创建公告", description = "创建新的公告")
    public Result<Map<String, Long>> createAnnouncement(@Valid @RequestBody AnnouncementCreateRequest request) {
        log.info("创建公告，参数：{}", request);
        Long id = announcementService.createAnnouncement(request);
        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasAuthority('content:announcement:update')")
    @Operation(summary = "更新公告", description = "更新公告信息")
    public Result<Void> updateAnnouncement(
            @Parameter(description = "公告ID") @PathVariable Long id,
            @Valid @RequestBody AnnouncementCreateRequest request) {
        log.info("更新公告，ID：{}，参数：{}", id, request);
        boolean success = announcementService.updateAnnouncement(id, request);
        if (success) {
            return Result.success();
        }
        return Result.error("更新失败");
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('content:announcement:delete')")
    @Operation(summary = "删除公告", description = "删除公告")
    public Result<Void> deleteAnnouncement(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        log.info("删除公告，ID：{}", id);
        boolean success = announcementService.deleteAnnouncement(id);
        if (success) {
            return Result.success();
        }
        return Result.error("删除失败");
    }

    @PutMapping("/{id}/status")
    // @PreAuthorize("hasAuthority('content:announcement:update')")
    @Operation(summary = "更新公告状态", description = "更新公告状态")
    public Result<Void> updateAnnouncementStatus(
            @Parameter(description = "公告ID") @PathVariable Long id,
            @Parameter(description = "状态：0-草稿，1-发布，2-下线") @RequestParam Integer status) {
        log.info("更新公告状态，ID：{}，状态：{}", id, status);
        boolean success = announcementService.updateStatus(id, status);
        if (success) {
            return Result.success();
        }
        return Result.error("更新状态失败");
    }

    @PutMapping("/{id}/publish")
    // @PreAuthorize("hasAuthority('content:announcement:publish')")
    @Operation(summary = "发布公告", description = "发布公告")
    public Result<Void> publishAnnouncement(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        log.info("发布公告，ID：{}", id);
        boolean success = announcementService.publishAnnouncement(id);
        if (success) {
            return Result.success();
        }
        return Result.error("发布失败");
    }

    @PutMapping("/{id}/offline")
    // @PreAuthorize("hasAuthority('content:announcement:update')")
    @Operation(summary = "下线公告", description = "下线公告")
    public Result<Void> offlineAnnouncement(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        log.info("下线公告，ID：{}", id);
        boolean success = announcementService.offlineAnnouncement(id);
        if (success) {
            return Result.success();
        }
        return Result.error("下线失败");
    }

    @PutMapping("/{id}/enabled")
    // @PreAuthorize("hasAuthority('content:announcement:update')")
    @Operation(summary = "切换启用状态", description = "切换公告启用状态，只有已发布的公告才能进行启用/禁用切换")
    public Result<Void> updateAnnouncementEnabled(
            @Parameter(description = "公告ID") @PathVariable Long id,
            @Parameter(description = "启用状态：0-禁用（隐藏），1-启用（显示）") @RequestParam Integer enabled) {
        log.info("更新公告启用状态，ID：{}，启用状态：{}", id, enabled);
        
        // 参数验证
        if (enabled == null || (enabled != 0 && enabled != 1)) {
            return Result.error("启用状态参数错误，必须为0或1");
        }
        
        boolean success = announcementService.updateEnabled(id, enabled);
        if (success) {
            return Result.success();
        }
        return Result.error("更新启用状态失败");
    }

    @GetMapping("/enabled")
    // @PreAuthorize("hasAuthority('content:announcement:query')")
    @Operation(summary = "查询启用的公告", description = "查询已发布且启用的公告列表，用于小程序端展示")
    public Result<IPage<AnnouncementResponse>> getEnabledAnnouncements(@Valid AnnouncementQueryRequest request) {
        log.info("获取启用的公告列表，参数：{}", request);
        IPage<AnnouncementResponse> result = announcementService.getEnabledAnnouncementList(request);
        return Result.success(result);
    }
}
