package com.lynn.museum.system.controller;

import com.lynn.museum.system.dto.UserStatisticsResponse;
import com.lynn.museum.system.service.UserStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户统计信息控制器
 * @author lynn
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/user-statistics")
@Tag(name = "UserStatisticsController", description = "用户统计信息接口")
public class UserStatisticsController {

    private final UserStatisticsService userStatisticsService;

    @GetMapping
    @Operation(summary = "获取用户统计信息")
    public UserStatisticsResponse getUserStatistics(
            @Parameter(description = "统计天数", example = "30")
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        log.info("获取用户统计信息, days: {}", days);
        return userStatisticsService.getUserStatistics(days);
    }
}
