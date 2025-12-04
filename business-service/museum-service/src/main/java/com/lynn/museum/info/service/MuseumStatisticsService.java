package com.lynn.museum.info.service;

import com.lynn.museum.info.dto.MuseumStatisticsResponse;

import java.util.List;
import java.util.Map;

/**
 * 博物馆统计信息服务接口
 */
public interface MuseumStatisticsService {

    /**
     * 获取博物馆统计信息
     *
     * @param days 统计天数
     * @return 博物馆统计信息
     */
    MuseumStatisticsResponse getMuseumStatistics(Integer days);
    
    /**
     * 按省份统计博物馆数量
     *
     * @return 省份统计列表 [{name: "北京市", value: 25}, ...]
     */
    List<Map<String, Object>> getMuseumCountByProvince();
    
    /**
     * 按城市统计博物馆数量
     *
     * @param provinceCode 省份编码（可选）
     * @return 城市统计列表 [{name: "北京市", value: 50}, ...]
     */
    List<Map<String, Object>> getMuseumCountByCity(String provinceCode);
}
