package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 博物馆统计信息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "博物馆统计信息响应")
public class MuseumStatisticsResponse {

    @Schema(description = "总博物馆数")
    private Long totalMuseums;

    @Schema(description = "开放中博物馆数")
    private Long activeMuseums;

    @Schema(description = "维护中博物馆数")
    private Long maintenanceMuseums;

    @Schema(description = "今日访客数")
    private Long visitorsToday;

    @Schema(description = "本周访客数")
    private Long visitorsWeek;

    @Schema(description = "本月访客数")
    private Long visitorsMonth;

    @Schema(description = "本年访客数")
    private Long visitorsYear;

    @Schema(description = "月访客趋势")
    private List<VisitorsTrend> visitorsTrend;

    @Schema(description = "展览类别分布")
    private List<CategoryDistribution> categoryDistribution;

    @Schema(description = "热门博物馆")
    private List<TopMuseum> topMuseums;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "访客趋势")
    public static class VisitorsTrend {
        @Schema(description = "月份")
        private String month;

        @Schema(description = "访客数")
        private Long visitors;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "类别分布")
    public static class CategoryDistribution {
        @Schema(description = "类别名称")
        private String category;

        @Schema(description = "博物馆数量")
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "热门博物馆")
    public static class TopMuseum {
        @Schema(description = "博物馆ID")
        private Long id;

        @Schema(description = "博物馆名称")
        private String name;

        @Schema(description = "访客数")
        private Long visitors;

        @Schema(description = "状态：0-关闭，1-开放")
        private Integer status;

        @Schema(description = "容量使用率")
        private Integer capacityUsage;
    }
}
