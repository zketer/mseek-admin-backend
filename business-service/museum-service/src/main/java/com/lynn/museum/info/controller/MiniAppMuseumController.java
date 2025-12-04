package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.CategoryResponse;
import com.lynn.museum.info.dto.MuseumResponse;
import com.lynn.museum.info.dto.NearbyMuseumsResponse;
import com.lynn.museum.info.model.entity.MuseumInfo;
import com.lynn.museum.info.service.AmapGeocodeService;
import com.lynn.museum.info.service.MiniappMuseumService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序博物馆控制器
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "MiniAppMuseumController", description = "小程序博物馆相关接口")
@RestController
@RequestMapping("/miniapp/museums")
@RequiredArgsConstructor
@Slf4j
public class MiniAppMuseumController {

    private final MiniappMuseumService miniappMuseumService;
    private final AmapGeocodeService amapGeocodeService;

    @Operation(summary = "分页查询博物馆列表", description = "小程序端分页查询博物馆列表，支持城市筛选和关键词搜索")
    @GetMapping
    public Result<IPage<MuseumResponse>> getMuseumPage(
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "城市代码") @RequestParam(required = false) String cityCode,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "分类ID") @RequestParam(required = false) Integer categoryId,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "default") String sortBy) {
        
        log.info("小程序分页查询博物馆列表 - 页码：{}, 大小：{}, 城市：{}, 关键词：{}, 分类ID：{}, 排序：{}", 
                page, pageSize, cityCode, keyword, categoryId, sortBy);

        IPage<MuseumResponse> result = miniappMuseumService.getMuseumPage(page, pageSize, cityCode, keyword, categoryId, sortBy);
        return Result.success(result);
    }

    @Operation(summary = "根据城市获取博物馆列表", description = "根据城市代码获取该城市的所有博物馆")
    @GetMapping("/city/{cityCode}")
    public Result<IPage<MuseumResponse>> getMuseumsByCity(
            @Parameter(description = "城市代码") @PathVariable String cityCode,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "default") String sortBy) {
        
        log.info("根据城市获取博物馆列表 - 城市：{}, 页码：{}, 大小：{}, 关键词：{}, 排序：{}", 
                cityCode, page, pageSize, keyword, sortBy);

        IPage<MuseumResponse> result = miniappMuseumService.getMuseumsByCity(cityCode, page, pageSize, keyword, sortBy);
        return Result.success(result);
    }

    @Operation(summary = "搜索博物馆", description = "根据关键词搜索博物馆")
    @GetMapping("/search")
    public Result<IPage<MuseumResponse>> searchMuseums(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "当前页码") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "20") Integer pageSize,
            @Parameter(description = "城市代码") @RequestParam(required = false) String cityCode,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "relevance") String sortBy) {
        
        log.info("搜索博物馆 - 关键词：{}, 页码：{}, 大小：{}, 城市：{}, 排序：{}", 
                keyword, page, pageSize, cityCode, sortBy);

        IPage<MuseumResponse> result = miniappMuseumService.searchMuseums(keyword, page, pageSize, cityCode, sortBy);
        return Result.success(result);
    }

    @Operation(summary = "获取博物馆分类列表", description = "获取所有可用的博物馆分类")
    @GetMapping("/categories")
    public Result<List<CategoryResponse>> getCategories() {
        log.info("获取博物馆分类列表");
        
        List<CategoryResponse> categories = miniappMuseumService.getCategories();
        return Result.success(categories);
    }

    @Operation(summary = "获取博物馆详情", description = "根据博物馆ID获取详情信息")
    @GetMapping("/{id}")
    public Result<MuseumResponse> getMuseumDetail(
            @Parameter(description = "博物馆ID") @PathVariable Long id) {
        
        log.info("获取博物馆详情 - ID：{}", id);
        
        MuseumResponse museum = miniappMuseumService.getMuseumDetail(id);
        return Result.success(museum);
    }

    @Operation(summary = "根据位置获取附近博物馆", description = "根据经纬度获取附近的博物馆列表，包含位置信息，支持按名称搜索")
    @GetMapping("/nearby")
    public Result<NearbyMuseumsResponse> getNearbyMuseums(
            @Parameter(description = "纬度") @RequestParam Double latitude,
            @Parameter(description = "经度") @RequestParam Double longitude,
            @Parameter(description = "搜索半径(km)") @RequestParam(defaultValue = "20") Integer radius,
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "博物馆名称搜索") @RequestParam(required = false) String name,
            @Parameter(description = "城市编码（可选，如果提供则不重新调用高德API）") @RequestParam(required = false) String cityCode,
            @Parameter(description = "城市名称（可选）") @RequestParam(required = false) String cityName) {

        log.info("获取附近博物馆 - 位置：{},{}, 半径：{}km, 页码：{}, 大小：{}, 搜索：{}, 城市：{}({})",
                latitude, longitude, radius, page, pageSize, name, cityName, cityCode);

        NearbyMuseumsResponse result = miniappMuseumService.getNearbyMuseumsWithLocation(
                latitude, longitude, radius, page, pageSize, name, cityCode, cityName);
        return Result.success(result);
    }

    @Operation(summary = "分页获取热门博物馆列表", description = "根据用户打卡次数统计最热门的博物馆，支持懒加载分页和按名称搜索")
    @GetMapping("/hot")
    public Result<IPage<MuseumResponse>> getHotMuseums(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "5") Integer pageSize,
            @Parameter(description = "博物馆名称") @RequestParam(required = false) String name) {

        log.info("分页获取热门博物馆列表 - 页码：{}，页面大小：{}，名称：{}", page, pageSize, name);

        IPage<MuseumResponse> result = miniappMuseumService.getHotMuseums(page, pageSize, name);
        return Result.success(result);
    }
}
