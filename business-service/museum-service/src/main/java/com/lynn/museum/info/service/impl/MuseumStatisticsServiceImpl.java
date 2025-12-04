package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lynn.museum.info.dto.MuseumStatisticsResponse;
import com.lynn.museum.info.mapper.AreaCityMapper;
import com.lynn.museum.info.mapper.CheckinRecordMapper;
import com.lynn.museum.info.mapper.MuseumCategoryMapper;
import com.lynn.museum.info.mapper.MuseumInfoMapper;
import com.lynn.museum.info.model.entity.AreaCity;
import com.lynn.museum.info.model.entity.CheckinRecord;
import com.lynn.museum.info.model.entity.MuseumCategory;
import com.lynn.museum.info.model.entity.MuseumInfo;
import com.lynn.museum.info.service.MuseumStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 博物馆统计信息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MuseumStatisticsServiceImpl implements MuseumStatisticsService {

    private final MuseumInfoMapper museumInfoMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final MuseumCategoryMapper museumCategoryMapper;
    private final AreaCityMapper areaCityMapper;

    @Override
    public MuseumStatisticsResponse getMuseumStatistics(Integer days) {
        // 获取总博物馆数（只统计display=1的）
        Long totalMuseums = museumInfoMapper.selectCount(new LambdaQueryWrapper<MuseumInfo>()
                .eq(MuseumInfo::getDeleted, 0)
                .eq(MuseumInfo::getDisplay, 1));

        // 获取开放中博物馆数（display=1 且 status=1）
        Long activeMuseums = museumInfoMapper.selectCount(new LambdaQueryWrapper<MuseumInfo>()
                .eq(MuseumInfo::getDeleted, 0)
                .eq(MuseumInfo::getDisplay, 1)
                .eq(MuseumInfo::getStatus, 1));

        // 计算维护中博物馆数（display=1 但 status=0）
        Long maintenanceMuseums = totalMuseums - activeMuseums;

        // 获取今日访客数
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Long visitorsToday = checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getDeleted, 0)
                .between(CheckinRecord::getCheckinTime, 
                    Date.from(todayStart.atZone(ZoneId.systemDefault()).toInstant()),
                    Date.from(todayEnd.atZone(ZoneId.systemDefault()).toInstant())));

        // 获取本周访客数
        LocalDateTime weekStart = todayStart.minusDays(todayStart.getDayOfWeek().getValue() - 1);
        Long visitorsWeek = checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getDeleted, 0)
                .between(CheckinRecord::getCheckinTime, weekStart, todayEnd));

        // 获取本月访客数
        LocalDateTime monthStart = todayStart.withDayOfMonth(1);
        Long visitorsMonth = checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getDeleted, 0)
                .between(CheckinRecord::getCheckinTime, monthStart, todayEnd));

        // 获取本年访客数
        LocalDateTime yearStart = todayStart.withDayOfYear(1);
        Long visitorsYear = checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getDeleted, 0)
                .between(CheckinRecord::getCheckinTime, yearStart, todayEnd));

        // 获取月访客趋势
        List<MuseumStatisticsResponse.VisitorsTrend> visitorsTrend = getVisitorsTrend();

        // 获取展览类别分布
        List<MuseumStatisticsResponse.CategoryDistribution> categoryDistribution = getCategoryDistribution();

        // 获取热门博物馆
        List<MuseumStatisticsResponse.TopMuseum> topMuseums = getTopMuseums();

        // 构建并返回响应
        return MuseumStatisticsResponse.builder()
                .totalMuseums(totalMuseums)
                .activeMuseums(activeMuseums)
                .maintenanceMuseums(maintenanceMuseums)
                .visitorsToday(visitorsToday)
                .visitorsWeek(visitorsWeek)
                .visitorsMonth(visitorsMonth)
                .visitorsYear(visitorsYear)
                .visitorsTrend(visitorsTrend)
                .categoryDistribution(categoryDistribution)
                .topMuseums(topMuseums)
                .build();
    }

    /**
     * 获取月访客趋势
     *
     * @return 月访客趋势
     */
    private List<MuseumStatisticsResponse.VisitorsTrend> getVisitorsTrend() {
        List<MuseumStatisticsResponse.VisitorsTrend> result = new ArrayList<>();
        
        // 获取当前年份
        int currentYear = LocalDate.now().getYear();
        DateTimeFormatter monthFormatter = DateTimeFormatter.ofPattern("M月");
        
        // 获取每月的访客数据
        for (int month = 1; month <= 12; month++) {
            LocalDateTime monthStart = LocalDateTime.of(currentYear, month, 1, 0, 0);
            
            // 计算月末
            LocalDateTime monthEnd;
            if (month == 12) {
                monthEnd = LocalDateTime.of(currentYear, month, 31, 23, 59, 59);
            } else {
                monthEnd = LocalDateTime.of(currentYear, month + 1, 1, 0, 0).minusSeconds(1);
            }
            
            // 如果是未来月份，跳过
            if (monthStart.isAfter(LocalDateTime.now())) {
                continue;
            }
            
            // 查询该月的访客数
            Long visitors = checkinRecordMapper.selectCount(new LambdaQueryWrapper<CheckinRecord>()
                    .eq(CheckinRecord::getDeleted, 0)
                    .between(CheckinRecord::getCheckinTime, monthStart, monthEnd));
            
            // 格式化月份名称
            String monthName = monthStart.format(monthFormatter);
            
            // 添加到结果列表
            result.add(new MuseumStatisticsResponse.VisitorsTrend(monthName, visitors));
        }
        
        return result;
    }

    /**
     * 获取展览类别分布
     *
     * @return 展览类别分布
     */
    private List<MuseumStatisticsResponse.CategoryDistribution> getCategoryDistribution() {
        // 获取所有博物馆分类
        List<MuseumCategory> categories = museumCategoryMapper.selectList(new LambdaQueryWrapper<MuseumCategory>()
                .eq(MuseumCategory::getDeleted, 0)
                .eq(MuseumCategory::getStatus, 1));
        
        List<MuseumStatisticsResponse.CategoryDistribution> result = new ArrayList<>();
        
        // 如果没有分类数据，返回空列表
        if (categories.isEmpty()) {
            return result;
        }
        
        // 使用自定义SQL查询每个分类下的博物馆数量
        // 这里需要通过 mapper 执行自定义SQL查询
        // 假设我们有一个方法可以查询分类及其博物馆数量
        List<Map<String, Object>> categoryStats = museumCategoryMapper.selectCategoryStatistics();
        
        if (categoryStats != null && !categoryStats.isEmpty()) {
            for (Map<String, Object> stat : categoryStats) {
                Long categoryId = ((Number) stat.get("category_id")).longValue();
                Long count = ((Number) stat.get("museum_count")).longValue();
                
                // 查找对应的分类名称
                String categoryName = categories.stream()
                        .filter(cat -> cat.getId().equals(categoryId))
                        .map(MuseumCategory::getName)
                        .findFirst()
                        .orElse("未知分类");
                
                if (count > 0) {
                    result.add(new MuseumStatisticsResponse.CategoryDistribution(categoryName, count));
                }
            }
        }
        
        return result;
    }

    /**
     * 获取热门博物馆
     *
     * @return 热门博物馆
     */
    private List<MuseumStatisticsResponse.TopMuseum> getTopMuseums() {
        // 获取今日打卡记录
        LocalDateTime todayStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime todayEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        List<CheckinRecord> todayRecords = checkinRecordMapper.selectList(new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getDeleted, 0)
                .between(CheckinRecord::getCheckinTime, todayStart, todayEnd));

        // 按博物馆ID分组统计打卡人数
        Map<Long, Long> museumVisitorsMap = todayRecords.stream()
                .collect(Collectors.groupingBy(CheckinRecord::getMuseumId, Collectors.counting()));

        // 获取热门博物馆信息
        List<MuseumStatisticsResponse.TopMuseum> topMuseums = new ArrayList<>();
        museumVisitorsMap.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .limit(5)
                .forEach(entry -> {
                    Long museumId = entry.getKey();
                    Long visitors = entry.getValue();

                    // 查询博物馆信息
                    MuseumInfo museumInfo = museumInfoMapper.selectById(museumId);
                    if (museumInfo != null) {
                        // 计算容量使用率
                        int capacityUsage = 0;
                        Integer capacity = museumInfo.getCapacity();
                        if (capacity != null && capacity > 0) {
                            capacityUsage = (int) (visitors * 100 / capacity);
                        }

                        topMuseums.add(MuseumStatisticsResponse.TopMuseum.builder()
                                .id(museumId)
                                .name(museumInfo.getName())
                                .visitors(visitors)
                                .status(museumInfo.getStatus())
                                .capacityUsage(capacityUsage)
                                .build());
                    }
                });

        // 如果没有数据，则返回空列表
        // 不添加模拟数据，保持数据真实性

        return topMuseums;
    }
    
    @Override
    public List<Map<String, Object>> getMuseumCountByProvince() {
        // 查询所有博物馆（只统计display=1的）
        List<MuseumInfo> museums = museumInfoMapper.selectList(
                new LambdaQueryWrapper<MuseumInfo>()
                        .eq(MuseumInfo::getDeleted, 0)
                        .eq(MuseumInfo::getDisplay, 1)
        );
        
        // 按省份分组统计
        Map<String, Long> provinceCountMap = museums.stream()
                .collect(Collectors.groupingBy(
                        MuseumInfo::getProvinceCode,
                        Collectors.counting()
                ));
        
        // 转换为前端需要的格式 [{name: "省份名称", code: "省份编码", value: 数量}]
        return provinceCountMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    String provinceCode = entry.getKey();
                    Long count = entry.getValue();
                    
                    // 从第一个博物馆获取省份名称
                    String provinceName = museums.stream()
                            .filter(m -> provinceCode.equals(m.getProvinceCode()))
                            .findFirst()
                            .map(m -> getProvinceNameFromCode(provinceCode))
                            .orElse(provinceCode);
                    
                    item.put("name", provinceName);
                    item.put("code", provinceCode);
                    item.put("value", count);
                    return item;
                })
                .sorted((a, b) -> ((Long) b.get("value")).compareTo((Long) a.get("value")))
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Map<String, Object>> getMuseumCountByCity(String provinceCode) {
        // 构建查询条件
        LambdaQueryWrapper<MuseumInfo> queryWrapper = new LambdaQueryWrapper<MuseumInfo>()
                .eq(MuseumInfo::getDeleted, 0)
                .eq(MuseumInfo::getDisplay, 1);
        
        // 如果指定了省份编码，则只查询该省份的博物馆
        if (provinceCode != null && !provinceCode.isEmpty()) {
            queryWrapper.eq(MuseumInfo::getProvinceCode, provinceCode);
        }
        
        List<MuseumInfo> museums = museumInfoMapper.selectList(queryWrapper);
        
        // 按城市分组统计
        Map<String, Long> cityCountMap = museums.stream()
                .collect(Collectors.groupingBy(
                        MuseumInfo::getCityCode,
                        Collectors.counting()
                ));
        
        // 转换为前端需要的格式
        return cityCountMap.entrySet().stream()
                .map(entry -> {
                    Map<String, Object> item = new java.util.HashMap<>();
                    String cityCode = entry.getKey();
                    Long count = entry.getValue();
                    
                    // 从第一个博物馆获取城市名称
                    String cityName = museums.stream()
                            .filter(m -> cityCode.equals(m.getCityCode()))
                            .findFirst()
                            .map(m -> getCityNameFromCode(cityCode))
                            .orElse(cityCode);
                    
                    item.put("name", cityName);
                    item.put("code", cityCode);
                    item.put("value", count);
                    return item;
                })
                .sorted((a, b) -> ((Long) b.get("value")).compareTo((Long) a.get("value")))
                .collect(Collectors.toList());
    }
    
    /**
     * 从省份编码获取省份名称
     */
    private String getProvinceNameFromCode(String provinceCode) {
        // 这里可以通过查询province表获取，暂时使用简单映射
        // TODO: 从province表查询省份名称
        Map<String, String> provinceMap = new java.util.HashMap<>();
        provinceMap.put("110000", "北京市");
        provinceMap.put("120000", "天津市");
        provinceMap.put("130000", "河北省");
        provinceMap.put("140000", "山西省");
        provinceMap.put("150000", "内蒙古自治区");
        provinceMap.put("210000", "辽宁省");
        provinceMap.put("220000", "吉林省");
        provinceMap.put("230000", "黑龙江省");
        provinceMap.put("310000", "上海市");
        provinceMap.put("320000", "江苏省");
        provinceMap.put("330000", "浙江省");
        provinceMap.put("340000", "安徽省");
        provinceMap.put("350000", "福建省");
        provinceMap.put("360000", "江西省");
        provinceMap.put("370000", "山东省");
        provinceMap.put("410000", "河南省");
        provinceMap.put("420000", "湖北省");
        provinceMap.put("430000", "湖南省");
        provinceMap.put("440000", "广东省");
        provinceMap.put("450000", "广西壮族自治区");
        provinceMap.put("460000", "海南省");
        provinceMap.put("500000", "重庆市");
        provinceMap.put("510000", "四川省");
        provinceMap.put("520000", "贵州省");
        provinceMap.put("530000", "云南省");
        provinceMap.put("540000", "西藏自治区");
        provinceMap.put("610000", "陕西省");
        provinceMap.put("620000", "甘肃省");
        provinceMap.put("630000", "青海省");
        provinceMap.put("640000", "宁夏回族自治区");
        provinceMap.put("650000", "新疆维吾尔自治区");
        provinceMap.put("710000", "台湾省");
        provinceMap.put("810000", "香港特别行政区");
        provinceMap.put("820000", "澳门特别行政区");
        
        return provinceMap.getOrDefault(provinceCode, provinceCode);
    }
    
    /**
     * 从城市编码获取城市名称
     */
    private String getCityNameFromCode(String cityCode) {
        if (cityCode == null || cityCode.isEmpty()) {
            return cityCode;
        }
        
        try {
            // 从area_cities表查询城市名称，使用adcode字段匹配
            AreaCity city = areaCityMapper.selectOne(new LambdaQueryWrapper<AreaCity>()
                    .eq(AreaCity::getAdcode, cityCode));
            
            if (city != null && city.getName() != null) {
                // 直接返回城市名称，地图数据中也是包含"市"的（如"成都市"）
                return city.getName();
            }
            
            log.warn("未找到城市编码对应的城市名称: {}", cityCode);
            return cityCode;
        } catch (Exception e) {
            log.error("查询城市名称失败，城市编码: {}", cityCode, e);
            return cityCode;
        }
    }
}
