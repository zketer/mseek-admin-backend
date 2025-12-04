package com.lynn.museum.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 系统概览响应DTO
 *
 * @author lynn
 * @since 2025-01-20
 */
@Data
@Schema(description = "系统概览响应")
public class SystemOverviewResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "系统基本信息")
    private SystemBasicInfo basicInfo;

    @Schema(description = "技术栈列表")
    private List<TechStackInfo> techStack;

    @Schema(description = "微服务列表")
    private List<MicroserviceInfo> microservices;

    @Schema(description = "功能模块列表")
    private List<FeatureModuleInfo> featureModules;

    @Schema(description = "开发计划列表")
    private List<DevelopmentPlanInfo> developmentPlans;

    @Schema(description = "第三方服务列表")
    private List<ThirdPartyServiceInfo> thirdPartyServices;

    /**
     * 系统基本信息
     */
    @Data
    @Schema(description = "系统基本信息")
    public static class SystemBasicInfo implements Serializable {
        @Schema(description = "系统名称")
        private String systemName;
        @Schema(description = "系统版本")
        private String systemVersion;
        @Schema(description = "架构模式")
        private String architectureMode;
        @Schema(description = "部署方式")
        private String deploymentMethod;
        @Schema(description = "技术栈")
        private String techStack;
        @Schema(description = "数据存储")
        private String dataStorage;
        @Schema(description = "服务治理")
        private String serviceGovernance;
        @Schema(description = "认证方案")
        private String authSolution;
        @Schema(description = "系统描述")
        private String systemDescription;
        @Schema(description = "系统状态消息")
        private String statusMessage;
    }

    /**
     * 技术栈信息
     */
    @Data
    @Schema(description = "技术栈信息")
    public static class TechStackInfo implements Serializable {
        @Schema(description = "分类")
        private String category;
        @Schema(description = "技术名称")
        private String name;
        @Schema(description = "版本号")
        private String version;
        @Schema(description = "描述")
        private String description;
        @Schema(description = "标签颜色")
        private String tagColor;
        @Schema(description = "端口号")
        private String port;
    }

    /**
     * 微服务信息
     */
    @Data
    @Schema(description = "微服务信息")
    public static class MicroserviceInfo implements Serializable {
        @Schema(description = "服务名称")
        private String serviceName;
        @Schema(description = "服务代码")
        private String serviceCode;
        @Schema(description = "端口号")
        private Integer port;
        @Schema(description = "服务描述")
        private String description;
        @Schema(description = "主要功能")
        private List<String> features;
        @Schema(description = "状态")
        private String status;
        @Schema(description = "状态标签颜色")
        private String statusTagColor;
    }

    /**
     * 功能模块信息
     */
    @Data
    @Schema(description = "功能模块信息")
    public static class FeatureModuleInfo implements Serializable {
        @Schema(description = "模块名称")
        private String moduleName;
        @Schema(description = "模块类型")
        private String moduleType;
        @Schema(description = "模块描述")
        private String description;
        @Schema(description = "完成进度")
        private Integer progress;
        @Schema(description = "标签文本")
        private String tagText;
        @Schema(description = "标签颜色")
        private String tagColor;
    }

    /**
     * 开发计划信息
     */
    @Data
    @Schema(description = "开发计划信息")
    public static class DevelopmentPlanInfo implements Serializable {
        @Schema(description = "计划类型")
        private String planType;
        @Schema(description = "计划标题")
        private String title;
        @Schema(description = "计划项目")
        private List<String> items;
    }

    /**
     * 第三方服务信息
     */
    @Data
    @Schema(description = "第三方服务信息")
    public static class ThirdPartyServiceInfo implements Serializable {
        @Schema(description = "服务名称")
        private String serviceName;
        @Schema(description = "服务类型")
        private String serviceType;
        @Schema(description = "状态")
        private String status;
        @Schema(description = "状态标签颜色")
        private String statusTagColor;
        @Schema(description = "服务描述")
        private String description;
    }
}

