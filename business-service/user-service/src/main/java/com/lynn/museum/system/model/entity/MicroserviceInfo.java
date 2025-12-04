package com.lynn.museum.system.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 微服务信息实体类
 *
 * @author lynn
 * @since 2025-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("microservice_info")
@Schema(description = "微服务信息")
public class MicroserviceInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    @Schema(description = "服务名称", example = "网关服务", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceName;

    @Schema(description = "服务代码", example = "gateway-service", requiredMode = Schema.RequiredMode.REQUIRED)
    private String serviceCode;

    @Schema(description = "端口号", example = "8000")
    private Integer port;

    @Schema(description = "服务描述", example = "智能路径发现、多层缓存、统一鉴权")
    private String description;

    @Schema(description = "主要功能（JSON数组格式）", example = "[\"智能路径发现\", \"多层缓存\"]")
    private String features;

    @Schema(description = "状态", example = "running", allowableValues = {"running", "stopped", "maintenance"})
    private String status;

    @Schema(description = "状态标签颜色", example = "green")
    private String statusTagColor;

    @Schema(description = "排序索引", example = "0")
    private Integer orderIndex;

    @TableLogic
    @Schema(description = "删除标志", example = "0")
    private Integer deleted;
}

