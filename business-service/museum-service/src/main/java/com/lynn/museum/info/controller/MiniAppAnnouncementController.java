package com.lynn.museum.info.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.AnnouncementResponse;
import com.lynn.museum.info.service.AnnouncementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序公告控制器
 */
@RestController
@RequestMapping("/miniapp/announcements")
@Tag(name = "MiniAppAnnouncementController", description = "小程序公告相关接口")
@Slf4j
public class MiniAppAnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    @GetMapping
    @Operation(summary = "获取有效公告列表", description = "获取当前有效的公告列表")
    public Result<List<AnnouncementResponse>> getActiveAnnouncements(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("小程序获取公告列表，限制数量：{}", limit);
        List<AnnouncementResponse> result = announcementService.getActiveAnnouncements(limit);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取公告详情", description = "获取公告详情并增加阅读次数")
    public Result<AnnouncementResponse> getAnnouncementDetail(
            @Parameter(description = "公告ID") @PathVariable Long id) {
        log.info("小程序获取公告详情，ID：{}", id);
        
        // 增加阅读次数
        announcementService.incrementReadCount(id);
        
        AnnouncementResponse result = announcementService.getAnnouncementDetail(id);
        return Result.success(result);
    }
}
