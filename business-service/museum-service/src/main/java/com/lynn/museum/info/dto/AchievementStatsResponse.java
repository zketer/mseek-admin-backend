package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 成就统计响应DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "成就统计响应")
public class AchievementStatsResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "总成就数量")
    private Integer totalAchievements;

    @Schema(description = "已解锁成就数量")
    private Integer unlockedAchievements;

    @Schema(description = "完成率（百分比）")
    private Integer completionRate;

    @Schema(description = "分类统计")
    private List<CategoryStats> categories;

    @Data
    @Schema(description = "分类统计")
    public static class CategoryStats {
        @Schema(description = "分类ID")
        private String id;

        @Schema(description = "分类名称")
        private String name;

        @Schema(description = "分类总数")
        private Integer count;

        @Schema(description = "已解锁数量")
        private Integer unlockedCount;
    }
}
