package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lynn.museum.info.dto.AchievementResponse;
import com.lynn.museum.info.dto.AchievementStatsResponse;
import com.lynn.museum.info.entity.Achievement;
import com.lynn.museum.info.entity.UserAchievement;
import com.lynn.museum.info.mapper.AchievementMapper;
import com.lynn.museum.info.mapper.UserAchievementMapper;
import com.lynn.museum.info.mapper.CheckinRecordMapper;
import com.lynn.museum.info.service.AchievementService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 成就服务实现类
 *
 * @author lynn
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AchievementServiceImpl implements AchievementService {

    private final AchievementMapper achievementMapper;
    private final UserAchievementMapper userAchievementMapper;
    private final CheckinRecordMapper checkinRecordMapper;

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");

    @Override
    public List<AchievementResponse> getUserAchievements(Long userId) {
        // 获取所有启用的成就
        List<Achievement> allAchievements = achievementMapper.selectList(
                new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getStatus, 1)
                        .orderByAsc(Achievement::getSortOrder)
        );

        if (allAchievements.isEmpty()) {
            log.warn("系统中暂无成就数据");
            return Collections.emptyList();
        }

        // 获取用户成就进度
        List<UserAchievement> userAchievements = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>()
                        .eq(UserAchievement::getUserId, userId)
        );

        Map<Long, UserAchievement> userAchievementMap = userAchievements.stream()
                .collect(Collectors.toMap(UserAchievement::getAchievementId, ua -> ua));

        // 组装响应数据
        return allAchievements.stream().map(achievement -> {
            UserAchievement userAchievement = userAchievementMap.get(achievement.getId());
            
            AchievementResponse response = new AchievementResponse();
            response.setId(achievement.getAchievementKey());
            response.setName(achievement.getName());
            response.setDescription(achievement.getDescription());
            response.setIcon(achievement.getIcon());
            response.setCategory(achievement.getCategory());
            response.setRequirement(achievement.getRequirement());
            response.setTarget(achievement.getTarget());
            response.setRarity(achievement.getRarity());

            if (userAchievement != null) {
                response.setProgress(userAchievement.getProgress());
                response.setUnlocked(userAchievement.getUnlocked() == 1);
                if (userAchievement.getUnlockedTime() != null) {
                    response.setUnlockedDate(DATE_FORMATTER.format(userAchievement.getUnlockedTime()));
                }
            } else {
                response.setProgress(0);
                response.setUnlocked(false);
            }

            return response;
        }).collect(Collectors.toList());
    }

    @Override
    public AchievementStatsResponse getAchievementStats(Long userId) {
        List<AchievementResponse> achievements = getUserAchievements(userId);
        
        AchievementStatsResponse stats = new AchievementStatsResponse();
        stats.setTotalAchievements(achievements.size());
        stats.setUnlockedAchievements((int) achievements.stream().filter(AchievementResponse::getUnlocked).count());
        
        if (achievements.size() > 0) {
            stats.setCompletionRate((stats.getUnlockedAchievements() * 100) / stats.getTotalAchievements());
        } else {
            stats.setCompletionRate(0);
        }

        // 分类统计
        Map<String, String> categoryNames = Map.of(
                "checkin", "打卡成就",
                "explore", "探索成就",
                "social", "社交成就",
                "special", "特殊成就"
        );

        List<AchievementStatsResponse.CategoryStats> categoryStats = achievements.stream()
                .collect(Collectors.groupingBy(AchievementResponse::getCategory))
                .entrySet().stream()
                .map(entry -> {
                    String category = entry.getKey();
                    List<AchievementResponse> categoryAchievements = entry.getValue();
                    
                    AchievementStatsResponse.CategoryStats categoryState = new AchievementStatsResponse.CategoryStats();
                    categoryState.setId(category);
                    categoryState.setName(categoryNames.getOrDefault(category, category));
                    categoryState.setCount(categoryAchievements.size());
                    categoryState.setUnlockedCount((int) categoryAchievements.stream().filter(AchievementResponse::getUnlocked).count());
                    
                    return categoryState;
                }).collect(Collectors.toList());

        // 添加"全部"分类
        AchievementStatsResponse.CategoryStats allStats = new AchievementStatsResponse.CategoryStats();
        allStats.setId("all");
        allStats.setName("全部");
        allStats.setCount(stats.getTotalAchievements());
        allStats.setUnlockedCount(stats.getUnlockedAchievements());
        categoryStats.add(0, allStats);

        stats.setCategories(categoryStats);
        return stats;
    }

    @Override
    @Transactional
    public List<AchievementResponse> checkAndUnlockAchievements(Long userId) {
        log.info("检查并解锁用户成就，用户ID：{}", userId);

        List<AchievementResponse> newUnlockedAchievements = new ArrayList<>();
        
        // 确保用户成就数据已初始化
        initUserAchievements(userId);
        
        // 检查打卡成就
        checkCheckinAchievements(userId, newUnlockedAchievements);
        
        // 检查社交成就
        checkSocialAchievements(userId, newUnlockedAchievements);
        
        if (!newUnlockedAchievements.isEmpty()) {
            log.info("解锁成就: userId={}, count={}", userId, newUnlockedAchievements.size());
        }
        return newUnlockedAchievements;
    }
    
    /**
     * 根据用户打卡记录检查打卡成就
     */
    private void checkCheckinAchievements(Long userId, List<AchievementResponse> newUnlockedAchievements) {
        try {
            // 1. 查询用户打卡记录数量（仅统计审核通过的记录）
            Integer checkinCount = checkinRecordMapper.countUserCheckins(userId);
            
            // 2. 查询用户打卡的不同博物馆数量
            Integer uniqueMuseumCount = checkinRecordMapper.countUserUniqueMuseums(userId);
            
            // 3. 获取所有打卡相关的成就
            List<Achievement> checkinAchievements = achievementMapper.selectList(
                    new LambdaQueryWrapper<Achievement>()
                            .eq(Achievement::getStatus, 1)
                            .like(Achievement::getAchievementKey, "checkin")
                            .orderByAsc(Achievement::getTarget)
            );
            
            // 4. 检查每个打卡成就是否达成
            for (Achievement achievement : checkinAchievements) {
                // 查询用户当前成就状态
                UserAchievement userAchievement = userAchievementMapper.selectOne(
                        new LambdaQueryWrapper<UserAchievement>()
                                .eq(UserAchievement::getUserId, userId)
                                .eq(UserAchievement::getAchievementId, achievement.getId())
                );
                
                if (userAchievement != null && userAchievement.getUnlocked() != 1) {
                    // 更新进度
                    userAchievement.setProgress(checkinCount);
                    
                    // 检查是否达成成就条件
                    if (checkinCount >= achievement.getTarget()) {
                        userAchievement.setUnlocked(1);
                        userAchievement.setUnlockedTime(new Date());
                        log.info("解锁成就: userId={}, achievement={}", userId, achievement.getName());
                        
                        // 构造返回的成就信息
                        AchievementResponse achievementResponse = new AchievementResponse();
                        achievementResponse.setId(achievement.getAchievementKey());
                        achievementResponse.setName(achievement.getName());
                        achievementResponse.setDescription(achievement.getDescription());
                        achievementResponse.setIcon(achievement.getIcon());
                        achievementResponse.setCategory(achievement.getCategory());
                        achievementResponse.setRequirement(achievement.getRequirement());
                        achievementResponse.setTarget(achievement.getTarget());
                        achievementResponse.setRarity(achievement.getRarity());
                        achievementResponse.setProgress(checkinCount);
                        achievementResponse.setUnlocked(true);
                        achievementResponse.setUnlockedDate(DATE_FORMATTER.format(new Date()));
                        
                        newUnlockedAchievements.add(achievementResponse);
                    }
                    
                    // 更新用户成就记录
                    userAchievementMapper.updateById(userAchievement);
                } else if (userAchievement != null && userAchievement.getUnlocked() == 1) {
                    // 已解锁的成就，仅更新进度
                    if (userAchievement.getProgress() < checkinCount) {
                        userAchievement.setProgress(checkinCount);
                        userAchievementMapper.updateById(userAchievement);
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("检查用户 {} 打卡成就时发生异常", userId, e);
        }
    }
    
    /**
     * 根据用户分享记录检查社交成就
     */
    private void checkSocialAchievements(Long userId, List<AchievementResponse> newUnlockedAchievements) {
        // TODO: 实现社交成就检查逻辑
    }

    @Override
    @Transactional
    public Boolean shareAchievement(Long userId, String achievementKey) {
        try {
            // 查找成就
            Achievement achievement = achievementMapper.selectOne(
                    new LambdaQueryWrapper<Achievement>()
                            .eq(Achievement::getAchievementKey, achievementKey)
                            .eq(Achievement::getStatus, 1)
            );

            if (achievement == null) {
                log.warn("成就不存在或已禁用：{}", achievementKey);
                return false;
            }

            // 查找用户成就记录
            UserAchievement userAchievement = userAchievementMapper.selectOne(
                    new LambdaQueryWrapper<UserAchievement>()
                            .eq(UserAchievement::getUserId, userId)
                            .eq(UserAchievement::getAchievementId, achievement.getId())
            );

            if (userAchievement == null || userAchievement.getUnlocked() != 1) {
                log.warn("用户尚未解锁该成就，无法分享：{}", achievementKey);
                return false;
            }

            // 更新分享状态
            userAchievement.setShared(1);
            userAchievement.setSharedTime(new Date());
            userAchievementMapper.updateById(userAchievement);
            
            log.info("分享成就: userId={}, achievementKey={}", userId, achievementKey);
            return true;

        } catch (Exception e) {
            log.error("分享成就失败", e);
            return false;
        }
    }

    @Override
    @Transactional
    public void initUserAchievements(Long userId) {
        // 获取所有启用的成就
        List<Achievement> achievements = achievementMapper.selectList(
                new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getStatus, 1)
        );

        // 检查用户是否已有成就记录
        List<UserAchievement> existingUserAchievements = userAchievementMapper.selectList(
                new LambdaQueryWrapper<UserAchievement>()
                        .eq(UserAchievement::getUserId, userId)
        );

        Set<Long> existingAchievementIds = existingUserAchievements.stream()
                .map(UserAchievement::getAchievementId)
                .collect(Collectors.toSet());

        // 为用户创建缺失的成就记录
        List<UserAchievement> newUserAchievements = achievements.stream()
                .filter(achievement -> !existingAchievementIds.contains(achievement.getId()))
                .map(achievement -> {
                    UserAchievement userAchievement = new UserAchievement();
                    userAchievement.setUserId(userId);
                    userAchievement.setAchievementId(achievement.getId());
                    
                    // 检查是否是注册成就，如果是则自动解锁
                    if ("register".equals(achievement.getAchievementKey())) {
                        userAchievement.setProgress(1);
                        userAchievement.setUnlocked(1);
                        userAchievement.setUnlockedTime(new Date());
                        log.info("解锁成就: userId={}, achievement=注册成就", userId);
                    } else {
                        userAchievement.setProgress(0);
                        userAchievement.setUnlocked(0);
                    }
                    
                    userAchievement.setShared(0);
                    return userAchievement;
                }).collect(Collectors.toList());

        if (!newUserAchievements.isEmpty()) {
            newUserAchievements.forEach(userAchievementMapper::insert);
        }
        
        // 检查已存在的用户是否需要补充解锁注册成就
        if (!existingUserAchievements.isEmpty()) {
            Achievement registerAchievement = achievements.stream()
                    .filter(achievement -> "register".equals(achievement.getAchievementKey()))
                    .findFirst()
                    .orElse(null);
                    
            if (registerAchievement != null) {
                UserAchievement existingRegisterAchievement = existingUserAchievements.stream()
                        .filter(ua -> ua.getAchievementId().equals(registerAchievement.getId()))
                        .findFirst()
                        .orElse(null);
                        
                // 如果注册成就存在但未解锁，则自动解锁
                if (existingRegisterAchievement != null && existingRegisterAchievement.getUnlocked() != 1) {
                    existingRegisterAchievement.setProgress(1);
                    existingRegisterAchievement.setUnlocked(1);
                    existingRegisterAchievement.setUnlockedTime(new Date());
                    userAchievementMapper.updateById(existingRegisterAchievement);
                    log.info("解锁成就: userId={}, achievement=注册成就", userId);
                }
            }
        }
    }

    @Override
    @Transactional
    public void updateAchievementProgress(Long userId, String achievementKey, Integer progress) {

        // 查找成就
        Achievement achievement = achievementMapper.selectOne(
                new LambdaQueryWrapper<Achievement>()
                        .eq(Achievement::getAchievementKey, achievementKey)
                        .eq(Achievement::getStatus, 1)
        );

        if (achievement == null) {
            log.warn("成就不存在或已禁用：{}", achievementKey);
            return;
        }

        // 查找或创建用户成就记录
        UserAchievement userAchievement = userAchievementMapper.selectOne(
                new LambdaQueryWrapper<UserAchievement>()
                        .eq(UserAchievement::getUserId, userId)
                        .eq(UserAchievement::getAchievementId, achievement.getId())
        );

        if (userAchievement == null) {
            // 创建新记录
            userAchievement = new UserAchievement();
            userAchievement.setUserId(userId);
            userAchievement.setAchievementId(achievement.getId());
            userAchievement.setProgress(progress);
            userAchievement.setUnlocked(0);
            userAchievement.setShared(0);
            userAchievementMapper.insert(userAchievement);
        } else {
            // 更新进度
            userAchievement.setProgress(progress);
            userAchievementMapper.updateById(userAchievement);
        }

        // 检查是否达成成就
        if (progress >= achievement.getTarget() && userAchievement.getUnlocked() != 1) {
            userAchievement.setUnlocked(1);
            userAchievement.setUnlockedTime(new Date());
            userAchievementMapper.updateById(userAchievement);
            log.info("解锁成就: userId={}, achievement={}", userId, achievement.getName());
        }
    }
}
