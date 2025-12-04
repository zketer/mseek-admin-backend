package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.AreaDivisionResponse;
import com.lynn.museum.info.dto.DistrictCreateRequest;
import com.lynn.museum.info.dto.DistrictUpdateRequest;
import com.lynn.museum.info.dto.DistrictResponse;
import com.lynn.museum.info.service.AreaDivisionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 区县管理控制器
 *
 * @author lynn
 */
@Slf4j
@RestController
@RequestMapping("/areas/districts")
@RequiredArgsConstructor
@Tag(name = "AreaDistrictController", description = "区县管理相关接口")
public class AreaDistrictController {

    private final AreaDivisionService areaDivisionService;

    @GetMapping("/page")
    @Operation(summary = "分页查询区县列表", description = "高性能分页查询所有区县信息，支持关键词和区域代码搜索")
    public Result<IPage<AreaDivisionResponse>> getDistrictsPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "区域代码搜索") @RequestParam(required = false) String adcode) {
        log.info("分页查询区县列表: current={}, pageSize={}, keyword={}, adcode={}", current, pageSize, keyword, adcode);
        IPage<AreaDivisionResponse> result = areaDivisionService.getDistrictsPage(current, pageSize, keyword, adcode);
        return Result.success(result);
    }

    @GetMapping("/city/{cityCode}")
    @Operation(summary = "根据城市获取区县列表", description = "根据城市代码获取该城市下的所有区县（用于懒加载）")
    public Result<List<AreaDivisionResponse>> getDistrictsByCity(
            @Parameter(description = "城市代码") @PathVariable String cityCode) {
        log.info("根据城市获取区县列表: {}", cityCode);
        List<AreaDivisionResponse> districts = areaDivisionService.getDistrictsByCity(cityCode);
        return Result.success(districts);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取区县详情", description = "根据区县ID获取详细信息")
    public Result<DistrictResponse> getDistrictById(
            @Parameter(description = "区县ID") @PathVariable Integer id) {
        log.info("根据ID获取区县详情：{}", id);
        DistrictResponse result = areaDivisionService.getDistrictById(id);
        return Result.success(result);
    }

    @GetMapping("/adcode/{adcode}")
    @Operation(summary = "根据区域代码获取区县详情", description = "根据区域代码获取区县详细信息")
    public Result<DistrictResponse> getDistrictByAdcode(
            @Parameter(description = "区域代码") @PathVariable String adcode) {
        log.info("根据区域代码获取区县详情：{}", adcode);
        DistrictResponse result = areaDivisionService.getDistrictByAdcode(adcode);
        return Result.success(result);
    }

    @PostMapping
    @Operation(summary = "创建区县", description = "创建新的区县信息")
    public Result<DistrictResponse> createDistrict(
            @Valid @RequestBody DistrictCreateRequest request) {
        log.info("创建区县：{}", request);
        DistrictResponse result = areaDivisionService.createDistrict(request);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新区县", description = "根据ID更新区县信息")
    public Result<DistrictResponse> updateDistrict(
            @Parameter(description = "区县ID") @PathVariable Integer id,
            @Valid @RequestBody DistrictUpdateRequest request) {
        log.info("更新区县：id={}, request={}", id, request);
        DistrictResponse result = areaDivisionService.updateDistrict(id, request);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除区县", description = "根据ID删除区县")
    public Result<Void> deleteDistrict(
            @Parameter(description = "区县ID") @PathVariable Integer id) {
        log.info("删除区县：{}", id);
        areaDivisionService.deleteDistrict(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除区县", description = "批量删除多个区县")
    public Result<Void> deleteDistricts(
            @Parameter(description = "区县ID列表") @RequestBody List<Integer> ids) {
        log.info("批量删除区县：{}", ids);
        areaDivisionService.deleteDistricts(ids);
        return Result.success();
    }
}
