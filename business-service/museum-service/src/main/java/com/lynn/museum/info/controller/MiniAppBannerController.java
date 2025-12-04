package com.lynn.museum.info.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.BannerResponse;
import com.lynn.museum.info.service.BannerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 小程序轮播图控制器
 * @author lynn
 */
@RestController
@RequestMapping("/miniapp/banners")
@Tag(name = "MiniAppBannerController", description = "小程序轮播图相关接口")
@Slf4j
public class MiniAppBannerController {

    @Autowired
    private BannerService bannerService;

    @GetMapping
    @Operation(summary = "获取首页轮播图", description = "获取有效的轮播图列表")
    public Result<List<BannerResponse>> getActiveBanners(
            @Parameter(description = "限制数量") @RequestParam(defaultValue = "5") Integer limit) {
        log.info("小程序获取轮播图列表，限制数量：{}", limit);
        List<BannerResponse> result = bannerService.getActiveBanners(limit);
        return Result.success(result);
    }

    @PostMapping("/{id}/click")
    @Operation(summary = "记录轮播图点击", description = "记录轮播图点击次数")
    public Result<Void> recordBannerClick(
            @Parameter(description = "轮播图ID") @PathVariable Long id) {
        log.info("记录轮播图点击，ID：{}", id);
        boolean success = bannerService.incrementClickCount(id);
        if (success) {
            return Result.success();
        }
        return Result.error("记录点击失败");
    }
}