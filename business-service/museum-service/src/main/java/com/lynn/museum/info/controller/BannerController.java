package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.BannerCreateRequest;
import com.lynn.museum.info.dto.BannerQueryRequest;
import com.lynn.museum.info.dto.BannerResponse;
import com.lynn.museum.info.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 横幅管理控制器
 *
 * @author lynn
 * @since 2024-12-16
 */
@Slf4j
@RestController
@RequestMapping("/banners")
@RequiredArgsConstructor
@Tag(name = "BannerController", description = "轮播图的增删改查等功能")
public class BannerController {

    private final BannerService bannerService;

    @GetMapping
    // @PreAuthorize("hasAuthority('content:banner:query')")
    @Operation(summary = "分页查询轮播图", description = "根据条件分页查询轮播图列表")
    public Result<IPage<BannerResponse>> getBanners(@Valid BannerQueryRequest query) {
        log.info("分页查询轮播图，查询条件: {}", query);
        IPage<BannerResponse> result = bannerService.getBanners(query);
        return Result.success(result);
    }

    @PostMapping
    // @PreAuthorize("hasAuthority('content:banner:create')")
    @Operation(summary = "创建轮播图", description = "创建新的轮播图")
    public Result<Map<String, Long>> createBanner(@Valid @RequestBody BannerCreateRequest request) {
        log.info("创建轮播图，请求参数: {}", request);
        Long id = bannerService.createBanner(request);
        Map<String, Long> result = new HashMap<>();
        result.put("id", id);
        return Result.success(result);
    }

    @PutMapping("/{id}")
    // @PreAuthorize("hasAuthority('content:banner:update')")
    @Operation(summary = "更新轮播图", description = "更新指定的轮播图信息")
    public Result<Void> updateBanner(
            @Parameter(description = "轮播图ID") @PathVariable Long id,
            @Valid @RequestBody BannerCreateRequest request) {
        log.info("更新轮播图，ID: {}, 请求参数: {}", id, request);
        bannerService.updateBanner(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('content:banner:delete')")
    @Operation(summary = "删除轮播图", description = "删除指定的轮播图")
    public Result<Void> deleteBanner(
            @Parameter(description = "轮播图ID") @PathVariable Long id) {
        log.info("删除轮播图，ID: {}", id);
        bannerService.deleteBanner(id);
        return Result.success();
    }

    @PutMapping("/{id}/status")
    // @PreAuthorize("hasAuthority('content:banner:update')")
    @Operation(summary = "更新轮播图状态", description = "更新轮播图的上下线状态")
    public Result<Void> updateBannerStatus(
            @Parameter(description = "轮播图ID") @PathVariable Long id,
            @Parameter(description = "状态：0-下线，1-上线") @RequestParam Integer status) {
        log.info("更新轮播图状态，ID: {}, 状态: {}", id, status);
        bannerService.updateBannerStatus(id, status);
        return Result.success();
    }
}

