package com.lynn.museum.system.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 系统架构实体类
 *
 * @author lynn
 * @since 2025-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_architecture")
@Schema(description = "系统架构信息")
public class SystemArchitecture extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /**
     * 架构类型：overview-总体架构，auth-认证架构，deployment-部署架构，data-数据架构
     */
    @Schema(description = "架构类型", example = "overview", requiredMode = Schema.RequiredMode.REQUIRED,
            allowableValues = {"overview", "auth", "deployment", "data"})
    private String type;

    /**
     * 架构标题
     */
    @Schema(description = "架构标题", example = "系统总体架构图", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    /**
     * Mermaid图表代码
     */
    @Schema(description = "Mermaid图表代码")
    private String mermaidCode;

    /**
     * 架构说明
     */
    @Schema(description = "架构说明")
    private String description;

    /**
     * 架构详细信息（JSON格式）
     */
    @Schema(description = "架构详细信息（JSON格式）")
    private String architectureDetails;

    /**
     * 排序索引
     */
    @Schema(description = "排序索引", example = "0")
    private Integer orderIndex;

    /**
     * 状态：active-激活，inactive-禁用
     */
    @Schema(description = "状态", example = "active", allowableValues = {"active", "inactive"})
    private String status;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @Schema(description = "删除标志", example = "0")
    private Integer deleted;
}

