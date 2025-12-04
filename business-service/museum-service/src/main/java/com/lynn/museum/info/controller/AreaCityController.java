package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.*;
import com.lynn.museum.info.service.AreaCityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 城市管理控制器
 *
 * @author lynn
 */
@Slf4j
@RestController
@RequestMapping("/areas/cities")
@RequiredArgsConstructor
@Tag(name = "AreaCityController", description = "城市管理相关接口")
public class AreaCityController {

    private final AreaCityService areaCityService;

    @GetMapping
    @Operation(summary = "分页查询城市列表", description = "支持按名称、区域代码、省份等条件查询")
    public Result<IPage<CityResponse>> getCityList(CityQueryRequest request) {
        log.info("分页查询城市列表：{}", request);
        IPage<CityResponse> result = areaCityService.getCityList(request);
        return Result.success(result);
    }

    @GetMapping("/province/{provinceAdcode}")
    @Operation(summary = "根据省份获取城市列表", description = "根据省份代码获取该省份下的所有城市")
    public Result<List<CityResponse>> getCitiesByProvince(
            @Parameter(description = "省份代码") @PathVariable String provinceAdcode) {
        log.info("根据省份获取城市列表：{}", provinceAdcode);
        List<CityResponse> result = areaCityService.getCitiesByProvince(provinceAdcode);
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取城市详情", description = "根据城市ID获取详细信息")
    public Result<CityResponse> getCityById(
            @Parameter(description = "城市ID") @PathVariable Integer id) {
        log.info("根据ID获取城市详情：{}", id);
        CityResponse result = areaCityService.getCityById(id);
        return Result.success(result);
    }

    @GetMapping("/adcode/{adcode}")
    @Operation(summary = "根据区域代码获取城市详情", description = "根据区域代码获取城市详细信息")
    public Result<CityResponse> getCityByAdcode(
            @Parameter(description = "区域代码") @PathVariable String adcode) {
        log.info("根据区域代码获取城市详情：{}", adcode);
        CityResponse result = areaCityService.getCityByAdcode(adcode);
        return Result.success(result);
    }

    @PostMapping
    @Operation(summary = "创建城市", description = "创建新的城市信息")
    public Result<CityResponse> createCity(
            @Valid @RequestBody CityCreateRequest request) {
        log.info("创建城市：{}", request);
        CityResponse result = areaCityService.createCity(request);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新城市", description = "根据ID更新城市信息")
    public Result<CityResponse> updateCity(
            @Parameter(description = "城市ID") @PathVariable Integer id,
            @Valid @RequestBody CityUpdateRequest request) {
        log.info("更新城市：id={}, request={}", id, request);
        CityResponse result = areaCityService.updateCity(id, request);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除城市", description = "根据ID删除城市")
    public Result<Void> deleteCity(
            @Parameter(description = "城市ID") @PathVariable Integer id) {
        log.info("删除城市：{}", id);
        areaCityService.deleteCity(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除城市", description = "批量删除多个城市")
    public Result<Void> deleteCities(
            @Parameter(description = "城市ID列表") @RequestBody List<Integer> ids) {
        log.info("批量删除城市：{}", ids);
        areaCityService.deleteCities(ids);
        return Result.success();
    }
}
