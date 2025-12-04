package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.info.entity.UserAchievement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户成就关联Mapper
 *
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface UserAchievementMapper extends BaseMapper<UserAchievement> {

    /**
     * 获取用户成就进度列表
     *
     * @param userId 用户ID
     * @return 用户成就进度列表
     */
    List<UserAchievement> getUserAchievements(@Param("userId") Long userId);

    /**
     * 获取用户成就统计
     *
     * @param userId 用户ID
     * @return 统计信息
     */
    UserAchievement getUserAchievementStats(@Param("userId") Long userId);
}
