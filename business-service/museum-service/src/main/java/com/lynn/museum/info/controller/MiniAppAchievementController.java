package com.lynn.museum.info.controller;

import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.AchievementResponse;
import com.lynn.museum.info.dto.AchievementStatsResponse;
import com.lynn.museum.info.service.AchievementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 小程序成就控制器
 *
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "MiniAppAchievementController", description = "小程序成就相关接口")
@RestController
@RequestMapping("/miniapp/achievements")
@RequiredArgsConstructor
@Slf4j
public class MiniAppAchievementController {

    private final AchievementService achievementService;

    @Operation(summary = "获取用户成就列表", description = "获取用户的所有成就及其完成情况")
    @GetMapping
    public Result<List<AchievementResponse>> getUserAchievements(
            @Parameter(description = "用户ID") @RequestHeader("userId") Long userId) {

        log.info("小程序获取用户成就列表，用户ID：{}", userId);

        // 初始化用户成就数据（如果是首次访问）
        achievementService.initUserAchievements(userId);

        List<AchievementResponse> achievements = achievementService.getUserAchievements(userId);
        return Result.success(achievements);
    }

    @Operation(summary = "获取成就统计信息", description = "获取用户成就完成统计")
    @GetMapping("/stats")
    public Result<AchievementStatsResponse> getAchievementStats(
            @Parameter(description = "用户ID") @RequestHeader("userId") Long userId) {

        log.info("小程序获取成就统计信息，用户ID：{}", userId);

        AchievementStatsResponse stats = achievementService.getAchievementStats(userId);
        return Result.success(stats);
    }

    @Operation(summary = "检查并解锁新成就", description = "检查用户是否达成新的成就条件并自动解锁")
    @PostMapping("/check")
    public Result<List<AchievementResponse>> checkAndUnlockAchievements(
            @Parameter(description = "用户ID") @RequestHeader("userId") Long userId) {

        log.info("小程序检查并解锁新成就，用户ID：{}", userId);

        List<AchievementResponse> newAchievements = achievementService.checkAndUnlockAchievements(userId);
        return Result.success(newAchievements);
    }

    @Operation(summary = "分享成就", description = "分享已解锁的成就")
    @PostMapping("/share")
    public Result<Boolean> shareAchievement(
            @Parameter(description = "用户ID") @RequestHeader("userId") Long userId,
            @RequestBody Map<String, String> request) {

        String achievementId = request.get("achievementId");
        log.info("小程序分享成就，用户ID：{}，成就ID：{}", userId, achievementId);

        Boolean success = achievementService.shareAchievement(userId, achievementId);
        return Result.success(success);
    }

    @Operation(summary = "更新成就进度", description = "内部接口：更新用户成就进度")
    @PostMapping("/progress")
    public Result<Void> updateAchievementProgress(
            @Parameter(description = "用户ID") @RequestHeader("userId") Long userId,
            @RequestBody Map<String, Object> request) {

        String achievementKey = (String) request.get("achievementKey");
        Integer progress = (Integer) request.get("progress");

        log.info("更新用户成就进度，用户ID：{}，成就标识：{}，进度：{}", userId, achievementKey, progress);

        achievementService.updateAchievementProgress(userId, achievementKey, progress);
        return Result.success();
    }
}
