package com.lynn.museum.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 系统架构响应DTO
 *
 * @author lynn
 * @since 2025-01-20
 */
@Data
@Schema(description = "系统架构响应")
public class SystemArchitectureResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "架构列表")
    private List<ArchitectureInfo> architectures;

    /**
     * 架构信息
     */
    @Data
    @Schema(description = "架构信息")
    public static class ArchitectureInfo implements Serializable {
        @Schema(description = "架构ID")
        private Long id;
        @Schema(description = "架构类型")
        private String type;
        @Schema(description = "架构标题")
        private String title;
        @Schema(description = "Mermaid图表代码")
        private String mermaidCode;
        @Schema(description = "架构说明")
        private String description;
        @Schema(description = "架构详细信息")
        private Object architectureDetails;
        @Schema(description = "排序索引")
        private Integer orderIndex;
        @Schema(description = "状态")
        private String status;
    }
}

