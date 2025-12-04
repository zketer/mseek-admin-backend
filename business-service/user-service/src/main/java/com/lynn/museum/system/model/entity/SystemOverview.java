package com.lynn.museum.system.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 系统概览实体类
 *
 * @author lynn
 * @since 2025-01-20
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("system_overview")
@Schema(description = "系统概览信息")
public class SystemOverview extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID", example = "1")
    private Long id;

    /**
     * 系统名称
     */
    @Schema(description = "系统名称", example = "文博探索·博物馆打卡系统", requiredMode = Schema.RequiredMode.REQUIRED)
    private String systemName;

    /**
     * 系统版本
     */
    @Schema(description = "系统版本", example = "v2.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private String systemVersion;

    /**
     * 架构模式
     */
    @Schema(description = "架构模式", example = "Spring Cloud微服务架构", requiredMode = Schema.RequiredMode.REQUIRED)
    private String architectureMode;

    /**
     * 部署方式
     */
    @Schema(description = "部署方式", example = "Docker + Kubernetes + Nacos", requiredMode = Schema.RequiredMode.REQUIRED)
    private String deploymentMethod;

    /**
     * 技术栈（JSON格式）
     */
    @Schema(description = "技术栈（JSON格式）", example = "[\"Java 17\", \"React 19\", \"Python 3.9\"]")
    private String techStack;

    /**
     * 数据存储
     */
    @Schema(description = "数据存储", example = "MySQL 8.0 + Redis 7.0")
    private String dataStorage;

    /**
     * 服务治理
     */
    @Schema(description = "服务治理", example = "Nacos + Spring Cloud Gateway")
    private String serviceGovernance;

    /**
     * 认证方案
     */
    @Schema(description = "认证方案", example = "RS256 JWT + JWKS动态密钥")
    private String authSolution;

    /**
     * 系统描述
     */
    @Schema(description = "系统描述")
    private String systemDescription;

    /**
     * 系统状态消息
     */
    @Schema(description = "系统状态消息")
    private String statusMessage;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @Schema(description = "删除标志", example = "0")
    private Integer deleted;
}

