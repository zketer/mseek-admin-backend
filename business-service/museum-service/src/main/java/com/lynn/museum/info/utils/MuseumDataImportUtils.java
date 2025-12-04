package com.lynn.museum.info.utils;

import com.lynn.museum.info.dto.MuseumCreateRequest;
import com.lynn.museum.info.model.entity.MuseumInfo;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

/**
 * 博物馆数据导入工具类
 * 用于处理抓取数据的字段映射和转换
 *
 * @author lynn
 * @since 2025-09-09
 */
@Slf4j
public class MuseumDataImportUtils {

    /**
     * 等级文字到数字的映射
     */
    private static final Map<String, Integer> LEVEL_MAPPING = new HashMap<>();
    
    static {
        LEVEL_MAPPING.put("一级", 1);
        LEVEL_MAPPING.put("二级", 2);
        LEVEL_MAPPING.put("三级", 3);
        LEVEL_MAPPING.put("四级", 4);
        LEVEL_MAPPING.put("五级", 5);
        LEVEL_MAPPING.put("未定级", 0);
    }

    /**
     * 将抓取的数据转换为创建请求DTO
     *
     * @param scrapedData 抓取的数据（JSON格式）
     * @return 创建请求DTO
     */
    public static MuseumCreateRequest convertToCreateRequest(Map<String, Object> scrapedData) {
        MuseumCreateRequest request = new MuseumCreateRequest();
        
        // 基础信息映射
        request.setName(getStringValue(scrapedData, "name"));
        request.setCode(generateMuseumCode(getStringValue(scrapedData, "name")));
        request.setProvinceCode(getStringValue(scrapedData, "province_code"));
        request.setCityCode(getStringValue(scrapedData, "city_code"));
        request.setDistrictCode(getStringValue(scrapedData, "district_code"));
        
        // 等级转换
        String levelStr = getStringValue(scrapedData, "level");
        request.setLevel(convertLevelToNumber(levelStr));
        
        // 新字段映射
        request.setType(getStringValue(scrapedData, "type"));
        request.setFreeAdmission(convertFreeAdmission(getStringValue(scrapedData, "free_admission")));
        request.setCollectionCount(getIntegerValue(scrapedData, "collection_count"));
        request.setPreciousItems(getIntegerValue(scrapedData, "precious_items"));
        request.setExhibitions(getIntegerValue(scrapedData, "exhibitions"));
        request.setEducationActivities(getIntegerValue(scrapedData, "education_activities"));
        // 访客数量类型从 BigDecimal 改为 Long
        BigDecimal visitorCountDecimal = getBigDecimalValue(scrapedData, "visitor_count");
        request.setVisitorCount(visitorCountDecimal != null ? visitorCountDecimal.longValue() : null);
        
        // 设置默认值
        // 默认开放状态
        request.setStatus(1);
        
        return request;
    }

    /**
     * 将抓取的数据转换为实体对象
     *
     * @param scrapedData 抓取的数据（JSON格式）
     * @return 博物馆实体对象
     */
    public static MuseumInfo convertToEntity(Map<String, Object> scrapedData) {
        MuseumInfo museum = new MuseumInfo();
        
        // 基础信息映射
        museum.setName(getStringValue(scrapedData, "name"));
        museum.setCode(generateMuseumCode(getStringValue(scrapedData, "name")));
        museum.setProvinceCode(getStringValue(scrapedData, "province_code"));
        museum.setCityCode(getStringValue(scrapedData, "city_code"));
        museum.setDistrictCode(getStringValue(scrapedData, "district_code"));
        
        // 等级转换
        String levelStr = getStringValue(scrapedData, "level");
        museum.setLevel(convertLevelToNumber(levelStr));
        
        // 新字段映射
        museum.setType(getStringValue(scrapedData, "type"));
        museum.setFreeAdmission(convertFreeAdmission(getStringValue(scrapedData, "free_admission")));
        museum.setCollectionCount(getIntegerValue(scrapedData, "collection_count"));
        museum.setPreciousItems(getIntegerValue(scrapedData, "precious_items"));
        museum.setExhibitions(getIntegerValue(scrapedData, "exhibitions"));
        museum.setEducationActivities(getIntegerValue(scrapedData, "education_activities"));
        // 访客数量类型从 BigDecimal 改为 Long
        BigDecimal visitorCountDecimal = getBigDecimalValue(scrapedData, "visitor_count");
        museum.setVisitorCount(visitorCountDecimal != null ? visitorCountDecimal.longValue() : null);
        
        // 设置默认值
        // 默认开放状态
        museum.setStatus(1);
        
        return museum;
    }

    /**
     * 生成博物馆编码
     *
     * @param name 博物馆名称
     * @return 博物馆编码
     */
    private static String generateMuseumCode(String name) {
        if (name == null || name.trim().isEmpty()) {
            return "MUSEUM_" + System.currentTimeMillis();
        }
        
        // 简单的编码生成逻辑，实际项目中可能需要更复杂的逻辑
        String code = name.replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "")
                .replaceAll("\\s+", "_")
                .toUpperCase();
        
        // 如果编码过长，截取前20个字符
        if (code.length() > 20) {
            code = code.substring(0, 20);
        }
        
        return "MUSEUM_" + code + "_" + System.currentTimeMillis() % 10000;
    }

    /**
     * 转换等级文字为数字
     *
     * @param levelStr 等级文字
     * @return 等级数字
     */
    private static Integer convertLevelToNumber(String levelStr) {
        if (levelStr == null || levelStr.trim().isEmpty()) {
            return 0;
        }
        return LEVEL_MAPPING.getOrDefault(levelStr.trim(), 0);
    }

    /**
     * 转换免费状态
     *
     * @param freeAdmissionStr 免费状态文字
     * @return 免费状态数字
     */
    private static Integer convertFreeAdmission(String freeAdmissionStr) {
        if (freeAdmissionStr == null || freeAdmissionStr.trim().isEmpty()) {
            return 0;
        }
        return "是".equals(freeAdmissionStr.trim()) ? 1 : 0;
    }

    /**
     * 安全获取字符串值
     */
    private static String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        return value != null ? value.toString() : null;
    }

    /**
     * 安全获取整数值
     */
    private static Integer getIntegerValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return 0;
        }
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse integer value for key: {}, value: {}", key, value);
            return 0;
        }
    }

    /**
     * 安全获取BigDecimal值
     */
    private static BigDecimal getBigDecimalValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof Number) {
            return BigDecimal.valueOf(((Number) value).doubleValue());
        }
        try {
            return new BigDecimal(value.toString());
        } catch (NumberFormatException e) {
            log.warn("Failed to parse BigDecimal value for key: {}, value: {}", key, value);
            return BigDecimal.ZERO;
        }
    }
}
