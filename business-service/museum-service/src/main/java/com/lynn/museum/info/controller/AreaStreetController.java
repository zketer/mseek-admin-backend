package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.StreetResponse;
import com.lynn.museum.info.dto.StreetCreateRequest;
import com.lynn.museum.info.dto.StreetUpdateRequest;
import com.lynn.museum.info.service.AreaStreetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 街道管理控制器
 *
 * @author lynn
 */
@Slf4j
@RestController
@RequestMapping("/areas/streets")
@RequiredArgsConstructor
@Tag(name = "AreaStreetController", description = "街道管理相关接口")
public class AreaStreetController {

    private final AreaStreetService areaStreetService;

    @GetMapping("/page")
    @Operation(summary = "分页查询街道列表", description = "高性能分页查询所有街道信息，支持关键词和区域代码搜索")
    public Result<IPage<StreetResponse>> getStreetsPage(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer current,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "关键词搜索") @RequestParam(required = false) String keyword,
            @Parameter(description = "区域代码搜索") @RequestParam(required = false) String adcode) {
        log.info("分页查询街道列表: current={}, pageSize={}, keyword={}, adcode={}", current, pageSize, keyword, adcode);
        IPage<StreetResponse> result = areaStreetService.getStreetsPage(current, pageSize, keyword, adcode);
        return Result.success(result);
    }

    @GetMapping("/district/{districtCode}")
    @Operation(summary = "根据区县获取街道列表", description = "根据区县代码获取该区县下的所有街道（用于懒加载）")
    public Result<List<StreetResponse>> getStreetsByDistrict(
            @Parameter(description = "区县代码") @PathVariable String districtCode) {
        log.info("根据区县获取街道列表: {}", districtCode);
        List<StreetResponse> streets = areaStreetService.getStreetsByDistrict(districtCode);
        return Result.success(streets);
    }

    @GetMapping("/{id}")
    @Operation(summary = "根据ID获取街道详情", description = "根据街道ID获取详细信息")
    public Result<StreetResponse> getStreetById(
            @Parameter(description = "街道ID") @PathVariable Integer id) {
        log.info("根据ID获取街道详情：{}", id);
        StreetResponse result = areaStreetService.getStreetById(id);
        return Result.success(result);
    }

    @GetMapping("/adcode/{adcode}")
    @Operation(summary = "根据区域代码获取街道详情", description = "根据区域代码获取街道详细信息")
    public Result<StreetResponse> getStreetByAdcode(
            @Parameter(description = "区域代码") @PathVariable String adcode) {
        log.info("根据区域代码获取街道详情：{}", adcode);
        StreetResponse result = areaStreetService.getStreetByAdcode(adcode);
        return Result.success(result);
    }

    @PostMapping
    @Operation(summary = "创建街道", description = "创建新的街道信息")
    public Result<StreetResponse> createStreet(
            @Valid @RequestBody StreetCreateRequest request) {
        log.info("创建街道：{}", request);
        StreetResponse result = areaStreetService.createStreet(request);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新街道", description = "根据ID更新街道信息")
    public Result<StreetResponse> updateStreet(
            @Parameter(description = "街道ID") @PathVariable Integer id,
            @Valid @RequestBody StreetUpdateRequest request) {
        log.info("更新街道：id={}, request={}", id, request);
        StreetResponse result = areaStreetService.updateStreet(id, request);
        return Result.success(result);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除街道", description = "根据ID删除街道")
    public Result<Void> deleteStreet(
            @Parameter(description = "街道ID") @PathVariable Integer id) {
        log.info("删除街道：{}", id);
        areaStreetService.deleteStreet(id);
        return Result.success();
    }

    @DeleteMapping("/batch")
    @Operation(summary = "批量删除街道", description = "批量删除多个街道")
    public Result<Void> deleteStreets(
            @Parameter(description = "区域ID列表") @RequestBody List<Integer> ids) {
        log.info("批量删除街道：{}", ids);
        areaStreetService.deleteStreets(ids);
        return Result.success();
    }
}
