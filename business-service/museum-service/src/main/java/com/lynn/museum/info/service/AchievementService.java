package com.lynn.museum.info.service;

import com.lynn.museum.info.dto.AchievementResponse;
import com.lynn.museum.info.dto.AchievementStatsResponse;

import java.util.List;

/**
 * 成就服务接口
 *
 * @author lynn
 * @since 2024-01-01
 */
public interface AchievementService {

    /**
     * 获取用户成就列表
     *
     * @param userId 用户ID
     * @return 成就列表
     */
    List<AchievementResponse> getUserAchievements(Long userId);

    /**
     * 获取用户成就统计信息
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    AchievementStatsResponse getAchievementStats(Long userId);

    /**
     * 检查并解锁用户成就
     *
     * @param userId 用户ID
     * @return 新解锁的成就列表
     */
    List<AchievementResponse> checkAndUnlockAchievements(Long userId);

    /**
     * 分享成就
     *
     * @param userId 用户ID
     * @param achievementKey 成就标识符
     * @return 是否分享成功
     */
    Boolean shareAchievement(Long userId, String achievementKey);

    /**
     * 初始化用户成就数据
     *
     * @param userId 用户ID
     */
    void initUserAchievements(Long userId);

    /**
     * 更新用户成就进度
     *
     * @param userId 用户ID
     * @param achievementKey 成就标识符
     * @param progress 新进度
     */
    void updateAchievementProgress(Long userId, String achievementKey, Integer progress);
}
