package com.lynn.museum.system.service;

import com.lynn.museum.system.dto.UserStatisticsResponse;

/**
 * 用户统计信息服务接口
 */
public interface UserStatisticsService {

    /**
     * 获取用户统计信息
     *
     * @param days 统计天数
     * @return 用户统计信息
     */
    UserStatisticsResponse getUserStatistics(Integer days);
}
