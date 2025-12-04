package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 省份打卡统计响应
 *
 * @author lynn
 * @since 2024-12-30
 */
@Data
@Schema(description = "省份打卡统计响应")
public class ProvinceCheckinStatsResponse {

    @Schema(description = "省份统计数据列表")
    private List<ProvinceStatsData> provinces;

    @Schema(description = "总统计信息")
    private OverallStats overall;

    @Data
    @Schema(description = "省份统计数据")
    public static class ProvinceStatsData {
        
        @Schema(description = "省份编码", example = "110000")
        private String provinceCode;
        
        @Schema(description = "省份名称", example = "北京")
        private String provinceName;
        
        @Schema(description = "该省份总博物馆数量", example = "180")
        private Integer totalMuseums;
        
        @Schema(description = "已访问博物馆数量", example = "5")
        private Integer visitedMuseums;
        
        @Schema(description = "是否已解锁（有打卡记录）", example = "true")
        private Boolean isUnlocked;
        
        @Schema(description = "打卡次数", example = "8")
        private Integer checkinCount;
        
        @Schema(description = "最后打卡时间", example = "2024-01-15 14:30:00")
        private String lastCheckinTime;

        @Schema(description = "已访问的博物馆列表")
        private List<VisitedMuseum> visitedMuseumList;
    }

    @Data
    @Schema(description = "已访问博物馆")
    public static class VisitedMuseum {
        
        @Schema(description = "博物馆ID", example = "1")
        private Long id;
        
        @Schema(description = "博物馆名称", example = "故宫博物院")
        private String name;
        
        @Schema(description = "打卡次数", example = "2")
        private Integer checkinCount;
        
        @Schema(description = "首次打卡时间", example = "2024-01-01 10:00:00")
        private String firstCheckinTime;
        
        @Schema(description = "最后打卡时间", example = "2024-01-15 14:30:00")
        private String lastCheckinTime;
    }

    @Data
    @Schema(description = "总统计信息")
    public static class OverallStats {
        
        @Schema(description = "已解锁省份数", example = "5")
        private Integer unlockedProvinces;
        
        @Schema(description = "全国总省份数", example = "34")
        private Integer totalProvinces;
        
        @Schema(description = "已访问国家级博物馆数", example = "12")
        private Integer visitedNationalMuseums;
        
        @Schema(description = "全国国家级博物馆总数", example = "130")
        private Integer totalNationalMuseums;
        
        @Schema(description = "覆盖率百分比", example = "14.7")
        private Double coverageRate;
    }
}
