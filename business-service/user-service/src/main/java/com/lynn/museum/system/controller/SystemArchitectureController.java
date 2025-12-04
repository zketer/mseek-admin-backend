package com.lynn.museum.system.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.system.dto.SystemArchitectureResponse;
import com.lynn.museum.system.service.SystemArchitectureService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 系统架构控制器
 *
 * @author lynn
 * @since 2025-01-20
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/system-architecture")
@Tag(name = "SystemArchitectureController", description = "系统架构接口")
public class SystemArchitectureController {

    private final SystemArchitectureService systemArchitectureService;

    @GetMapping
    @Operation(summary = "获取系统架构信息", description = "获取系统架构图表和详细信息")
    public Result<SystemArchitectureResponse> getSystemArchitecture() {
        log.info("获取系统架构信息");
        SystemArchitectureResponse response = systemArchitectureService.getSystemArchitecture();
        return Result.success(response);
    }
}

