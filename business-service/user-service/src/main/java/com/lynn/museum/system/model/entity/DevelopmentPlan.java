package com.lynn.museum.system.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 开发计划实体类
 *
 * @author lynn
 * @since 2025-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("development_plan")
@Schema(description = "开发计划")
public class DevelopmentPlan extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "计划类型", example = "short", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"short", "medium", "long"})
    private String planType;

    @Schema(description = "计划标题", example = "近期计划 (1-2个月)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "计划项目（JSON数组格式）", example = "[\"完成打卡功能核心逻辑\", \"实现用户积分和成就系统\"]",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String items;

    @Schema(description = "排序索引", example = "0")
    private Integer orderIndex;

    @TableLogic
    @Schema(description = "删除标志", example = "0")
    private Integer deleted;
}

