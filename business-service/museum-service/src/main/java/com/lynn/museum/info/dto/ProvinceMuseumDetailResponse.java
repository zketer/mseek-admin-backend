package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 省份博物馆详情响应
 *
 * @author lynn
 * @since 2024-12-30
 */
@Data
@Schema(description = "省份博物馆详情响应")
public class ProvinceMuseumDetailResponse {

    @Schema(description = "省份编码", example = "110000")
    private String provinceCode;
    
    @Schema(description = "省份名称", example = "北京")
    private String provinceName;
    
    @Schema(description = "该省份总博物馆数量", example = "180")
    private Integer totalMuseums;
    
    @Schema(description = "用户已访问博物馆数量", example = "5")
    private Integer visitedMuseums;
    
    @Schema(description = "博物馆列表")
    private List<MuseumDetailInfo> museums;
    
    @Schema(description = "城市统计列表")
    private List<CityStatsInfo> cities;

    @Data
    @Schema(description = "博物馆详细信息")
    public static class MuseumDetailInfo {
        
        @Schema(description = "博物馆ID", example = "1")
        private Long id;
        
        @Schema(description = "博物馆名称", example = "故宫博物院")
        private String name;
        
        @Schema(description = "详细地址", example = "北京市东城区景山前街4号")
        private String address;
        
        @Schema(description = "博物馆等级", example = "国家一级")
        private String level;
        
        @Schema(description = "博物馆类型", example = "历史文化")
        private String category;
        
        @Schema(description = "用户是否已访问", example = "true")
        private Boolean isVisited;
        
        @Schema(description = "首次访问时间", example = "2024-01-01 10:00:00")
        private String firstVisitDate;
        
        @Schema(description = "最后访问时间", example = "2024-01-15 14:30:00")
        private String lastVisitDate;
        
        @Schema(description = "用户打卡次数", example = "3")
        private Integer visitCount;
        
        @Schema(description = "开放时间", example = "08:30-17:00")
        private String openTime;
        
        @Schema(description = "门票价格", example = "60")
        private Integer ticketPrice;
        
        @Schema(description = "是否免费", example = "0")
        private Integer freeAdmission;
        
        @Schema(description = "描述信息", example = "明清两朝的皇家宫殿")
        private String description;
        
        @Schema(description = "博物馆状态", example = "1")
        private Integer status;
        
        @Schema(description = "城市名称", example = "北京市")
        private String cityName;
    }
    
    @Data
    @Schema(description = "城市统计信息")
    public static class CityStatsInfo {
        
        @Schema(description = "城市名称", example = "北京市")
        private String cityName;
        
        @Schema(description = "该城市总博物馆数量", example = "50")
        private Integer totalMuseums;
        
        @Schema(description = "用户已访问博物馆数量", example = "3")
        private Integer visitedMuseums;
        
        @Schema(description = "完成度百分比", example = "60")
        private Integer completionRate;
        
        @Schema(description = "是否已解锁（有打卡记录）", example = "true")
        private Boolean isUnlocked;
        
        @Schema(description = "最后打卡时间", example = "2024-01-15 14:30:00")
        private String lastCheckinTime;
        
        @Schema(description = "打卡次数", example = "5")
        private Integer checkinCount;
    }
}
