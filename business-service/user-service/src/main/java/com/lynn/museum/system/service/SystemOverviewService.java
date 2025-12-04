package com.lynn.museum.system.service;

import com.lynn.museum.system.dto.SystemOverviewResponse;

/**
 * 系统概览服务接口
 *
 * @author lynn
 * @since 2025-01-20
 */
public interface SystemOverviewService {

    /**
     * 获取系统概览信息
     *
     * @return 系统概览响应
     */
    SystemOverviewResponse getSystemOverview();
}

