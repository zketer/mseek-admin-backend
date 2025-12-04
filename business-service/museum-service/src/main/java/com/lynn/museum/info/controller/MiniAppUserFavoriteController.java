package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.ExhibitionResponse;
import com.lynn.museum.info.dto.MuseumResponse;
import com.lynn.museum.info.service.MiniAppUserFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户收藏控制器
 *
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/miniapp/favorites")
@RequiredArgsConstructor
@Tag(name = "MiniAppUserFavoriteController", description = "小程序用户收藏相关接口")
public class MiniAppUserFavoriteController {

    private final MiniAppUserFavoriteService miniAppUserFavoriteService;

    @PostMapping("/museum/{museumId}")
    @Operation(summary = "收藏博物馆", description = "用户收藏指定的博物馆")
    public Result<Boolean> favoriteMuseum(
            @Parameter(description = "博物馆ID", required = true) @PathVariable Long museumId,
            @Parameter(description = "用户ID", required = true) @RequestHeader("userId") Long userId
    ) {
        log.info("收藏博物馆请求 - 用户ID：{}, 博物馆ID：{}", userId, museumId);
        
        Boolean success = miniAppUserFavoriteService.favoriteMuseum(userId, museumId);
        return success ? Result.success(true) : Result.error("收藏失败，可能已经收藏过了");
    }

    @DeleteMapping("/museum/{museumId}")
    @Operation(summary = "取消收藏博物馆", description = "用户取消收藏指定的博物馆")
    public Result<Boolean> unfavoriteMuseum(
            @Parameter(description = "博物馆ID", required = true) @PathVariable Long museumId,
            @Parameter(description = "用户ID", required = true) @RequestHeader("userId") Long userId
    ) {
        log.info("取消收藏博物馆请求 - 用户ID：{}, 博物馆ID：{}", userId, museumId);
        
        Boolean success = miniAppUserFavoriteService.unfavoriteMuseum(userId, museumId);
        return success ? Result.success(true) : Result.error("取消收藏失败");
    }

    @PostMapping("/exhibition/{exhibitionId}")
    @Operation(summary = "收藏展览", description = "用户收藏指定的展览")
    public Result<Boolean> favoriteExhibition(
            @Parameter(description = "展览ID", required = true) @PathVariable Long exhibitionId,
            @Parameter(description = "用户ID", required = true) @RequestHeader("userId") Long userId
    ) {
        log.info("收藏展览请求 - 用户ID：{}, 展览ID：{}", userId, exhibitionId);
        
        Boolean success = miniAppUserFavoriteService.favoriteExhibition(userId, exhibitionId);
        return success ? Result.success(true) : Result.error("收藏失败，可能已经收藏过了");
    }

    @DeleteMapping("/exhibition/{exhibitionId}")
    @Operation(summary = "取消收藏展览", description = "用户取消收藏指定的展览")
    public Result<Boolean> unfavoriteExhibition(
            @Parameter(description = "展览ID", required = true) @PathVariable Long exhibitionId,
            @Parameter(description = "用户ID", required = true) @RequestHeader("userId") Long userId
    ) {
        log.info("取消收藏展览请求 - 用户ID：{}, 展览ID：{}", userId, exhibitionId);
        
        Boolean success = miniAppUserFavoriteService.unfavoriteExhibition(userId, exhibitionId);
        return success ? Result.success(true) : Result.error("取消收藏失败");
    }

    @GetMapping("/museums")
    @Operation(summary = "获取用户收藏的博物馆列表", description = "分页查询用户收藏的博物馆")
    public Result<IPage<MuseumResponse>> getUserFavoriteMuseums(
            @Parameter(description = "用户ID", required = true) @RequestHeader("userId") Long userId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页面大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "打卡状态：true-已打卡，false-未打卡") @RequestParam(required = false) Boolean visitStatus,
            @Parameter(description = "排序方式：time-收藏时间，name-名称，distance-距离") @RequestParam(defaultValue = "time") String sortBy
    ) {
        log.info("获取用户收藏博物馆列表 - 用户ID：{}, 页码：{}, 大小：{}", userId, page, pageSize);
        
        IPage<MuseumResponse> result = miniAppUserFavoriteService.getUserFavoriteMuseums(userId, page, pageSize, keyword, visitStatus, sortBy);
        return Result.success(result);
    }

    @GetMapping("/exhibitions")
    @Operation(summary = "获取用户收藏的展览列表", description = "分页查询用户收藏的展览")
    public Result<IPage<ExhibitionResponse>> getUserFavoriteExhibitions(
            @Parameter(description = "用户ID", required = true) @RequestHeader("userId") Long userId,
            @Parameter(description = "页码", example = "1") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页面大小", example = "10") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "展览状态：0-已结束，1-进行中，2-未开始") @RequestParam(required = false) Integer status,
            @Parameter(description = "排序方式：time-收藏时间，name-名称") @RequestParam(defaultValue = "time") String sortBy
    ) {
        log.info("获取用户收藏展览列表 - 用户ID：{}, 页码：{}, 大小：{}", userId, page, pageSize);
        
        IPage<ExhibitionResponse> result = miniAppUserFavoriteService.getUserFavoriteExhibitions(userId, page, pageSize, keyword, status, sortBy);
        return Result.success(result);
    }

    @GetMapping("/check/museum/{museumId}")
    @Operation(summary = "检查是否收藏博物馆", description = "检查用户是否收藏了指定博物馆")
    public Result<Boolean> checkMuseumFavorite(
            @Parameter(description = "博物馆ID", required = true) @PathVariable Long museumId,
            @Parameter(description = "用户ID", required = true) @RequestHeader("userId") Long userId
    ) {
        Boolean isFavorited = miniAppUserFavoriteService.isMuseumFavorited(userId, museumId);
        return Result.success(isFavorited);
    }

    @GetMapping("/check/exhibition/{exhibitionId}")
    @Operation(summary = "检查是否收藏展览", description = "检查用户是否收藏了指定展览")
    public Result<Boolean> checkExhibitionFavorite(
            @Parameter(description = "展览ID", required = true) @PathVariable Long exhibitionId,
            @Parameter(description = "用户ID", required = true) @RequestHeader("userId") Long userId
    ) {
        Boolean isFavorited = miniAppUserFavoriteService.isExhibitionFavorited(userId, exhibitionId);
        return Result.success(isFavorited);
    }

    @GetMapping("/stats")
    @Operation(summary = "获取用户收藏统计", description = "获取用户收藏的博物馆和展览统计信息")
    public Result<MiniAppUserFavoriteService.FavoriteStats> getUserFavoriteStats(
            @Parameter(description = "用户ID", required = true) @RequestHeader("userId") Long userId
    ) {
        log.info("获取用户收藏统计 - 用户ID：{}", userId);
        
        MiniAppUserFavoriteService.FavoriteStats stats = miniAppUserFavoriteService.getUserFavoriteStats(userId);
        return Result.success(stats);
    }
}
