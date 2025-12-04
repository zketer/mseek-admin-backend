package com.lynn.museum.system.service;

import com.lynn.museum.system.dto.SystemArchitectureResponse;

/**
 * 系统架构服务接口
 *
 * @author lynn
 * @since 2025-01-20
 */
public interface SystemArchitectureService {

    /**
     * 获取系统架构信息
     *
     * @return 系统架构响应
     */
    SystemArchitectureResponse getSystemArchitecture();
}

