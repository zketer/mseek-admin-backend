package com.lynn.museum.info.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.MuseumStatisticsResponse;
import com.lynn.museum.info.service.MuseumStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * 博物馆统计信息控制器
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/statistics")
@Tag(name = "MuseumStatisticsController", description = "博物馆统计信息接口")
public class MuseumStatisticsController {

    private final MuseumStatisticsService museumStatisticsService;

    @GetMapping
    @Operation(summary = "获取博物馆统计信息")
    public MuseumStatisticsResponse getMuseumStatistics(
            @Parameter(description = "统计天数", example = "30")
            @RequestParam(required = false, defaultValue = "30") Integer days) {
        log.info("获取博物馆统计信息, days: {}", days);
        return museumStatisticsService.getMuseumStatistics(days);
    }
    
    @GetMapping("/by-province")
    @Operation(summary = "按省份统计博物馆数量")
    public Result<List<Map<String, Object>>> getMuseumCountByProvince() {
        log.info("按省份统计博物馆数量");
        return Result.success(museumStatisticsService.getMuseumCountByProvince());
    }
    
    @GetMapping("/by-city")
    @Operation(summary = "按城市统计博物馆数量")
    public Result<List<Map<String, Object>>> getMuseumCountByCity(
            @Parameter(description = "省份编码", example = "110000")
            @RequestParam(required = false) String provinceCode) {
        log.info("按城市统计博物馆数量, provinceCode: {}", provinceCode);
        return Result.success(museumStatisticsService.getMuseumCountByCity(provinceCode));
    }
}
