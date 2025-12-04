package com.lynn.museum.system.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.system.dto.SystemOverviewResponse;
import com.lynn.museum.system.service.SystemOverviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统概览控制器
 *
 * @author lynn
 * @since 2025-01-20
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system-overview")
@Tag(name = "SystemOverviewController", description = "系统概览接口")
public class SystemOverviewController {

    private final SystemOverviewService systemOverviewService;

    @GetMapping
    @Operation(summary = "获取系统概览信息", description = "获取系统基本信息、技术栈、微服务、功能模块、开发计划等信息")
    public Result<SystemOverviewResponse> getSystemOverview() {
        log.info("获取系统概览信息");
        SystemOverviewResponse response = systemOverviewService.getSystemOverview();
        return Result.success(response);
    }
}

