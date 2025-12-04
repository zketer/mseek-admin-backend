package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.*;
import com.lynn.museum.info.service.AreaProvinceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 省份管理控制器
 *
 * @author lynn
 */
@Slf4j
@RestController
@RequestMapping("/areas/provinces")
@RequiredArgsConstructor
@Tag(name = "AreaProvinceController", description = "省份管理相关接口")
public class AreaProvinceController {

    private final AreaProvinceService areaProvinceService;

    @GetMapping
    @Operation(summary = "分页查询省份列表", description = "支持按名称、区域代码等条件查询")
    public Result<IPage<ProvinceResponse>> getProvinceList(ProvinceQueryRequest request) {
        log.info("分页查询省份列表：{}", request);
        IPage<ProvinceResponse> result = areaProvinceService.getProvinceList(request);
        return Result.success(result);
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有省份列表", description = "获取所有省份的基本信息（不分页）")
    public Result<List<ProvinceResponse>> getAllProvinces() {
        log.info("获取所有省份列表");
        List<ProvinceResponse> result = areaProvinceService.getAllProvinces();
        return Result.success(result);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取省份详情", description = "根据省份ID获取详细信息")
    public Result<ProvinceResponse> getProvinceById(
            @Parameter(description = "省份ID") @PathVariable Integer id) {
        log.info("根据ID获取省份详情：{}", id);
        ProvinceResponse result = areaProvinceService.getProvinceById(id);
        return Result.success(result);
    }

    @GetMapping("/adcode/{adcode}")
    @Operation(summary = "根据区域代码获取省份详情", description = "根据区域代码获取省份详细信息")
    public Result<ProvinceResponse> getProvinceByAdcode(
            @Parameter(description = "区域代码") @PathVariable String adcode) {
        log.info("根据区域代码获取省份详情：{}", adcode);
        ProvinceResponse result = areaProvinceService.getProvinceByAdcode(adcode);
        return Result.success(result);
    }

    @PostMapping
    @Operation(summary = "创建省份", description = "创建新的省份信息")
    public Result<ProvinceResponse> createProvince(
            @Valid @RequestBody ProvinceCreateRequest request) {
        log.info("创建省份：{}", request);
        ProvinceResponse result = areaProvinceService.createProvince(request);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新省份", description = "根据ID更新省份信息")
    public Result<ProvinceResponse> updateProvince(
            @Parameter(description = "省份ID") @PathVariable Integer id,
            @Valid @RequestBody ProvinceUpdateRequest request) {
        log.info("更新省份：id={}, request={}", id, request);
        ProvinceResponse result = areaProvinceService.updateProvince(id, request);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除省份", description = "根据ID删除省份")
    public Result<Void> deleteProvince(
            @Parameter(description = "省份ID") @PathVariable Integer id) {
        log.info("删除省份：{}", id);
        areaProvinceService.deleteProvince(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除省份", description = "批量删除多个省份")
    public Result<Void> deleteProvinces(
            @Parameter(description = "省份ID列表") @RequestBody List<Integer> ids) {
        log.info("批量删除省份：{}", ids);
        areaProvinceService.deleteProvinces(ids);
        return Result.success();
    }
}
