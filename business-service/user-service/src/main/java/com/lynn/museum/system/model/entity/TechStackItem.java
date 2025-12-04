package com.lynn.museum.system.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 技术栈项目实体类
 *
 * @author lynn
 * @since 2025-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("tech_stack_item")
@Schema(description = "技术栈项目")
public class TechStackItem extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "分类", example = "frontend", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"frontend", "backend", "database", "middleware", "other"})
    private String category;

    @Schema(description = "技术名称", example = "React", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @Schema(description = "版本号", example = "19")
    private String version;

    @Schema(description = "描述", example = "前端框架")
    private String description;

    @Schema(description = "标签颜色", example = "blue")
    private String tagColor;

    @Schema(description = "端口号", example = "8080")
    private String port;

    @Schema(description = "状态", example = "active", allowableValues = {"active", "deprecated"})
    private String status;

    @Schema(description = "排序索引", example = "0")
    private Integer orderIndex;

    @TableLogic
    @Schema(description = "删除标志", example = "0")
    private Integer deleted;
}

