package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 成就响应DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "成就响应")
public class AchievementResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "成就唯一标识符")
    private String id;

    @Schema(description = "成就名称")
    private String name;

    @Schema(description = "成就描述")
    private String description;

    @Schema(description = "成就图标")
    private String icon;

    @Schema(description = "成就分类")
    private String category;

    @Schema(description = "解锁要求描述")
    private String requirement;

    @Schema(description = "当前进度")
    private Integer progress;

    @Schema(description = "目标数值")
    private Integer target;

    @Schema(description = "是否已解锁")
    private Boolean unlocked;

    @Schema(description = "解锁时间")
    private String unlockedDate;

    @Schema(description = "稀有度")
    private String rarity;
}
