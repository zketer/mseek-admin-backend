package com.lynn.museum.info.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

/**
 * 应用版本实体类
 *
 * @author lynn
 * @since 2025-10-27
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("app_versions")
@Schema(description = "应用版本信息")
public class AppVersion extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "版本ID", example = "1")
    private Long id;

    /**
     * 版本名称，如 1.0.0
     */
    @Schema(description = "版本名称", example = "1.0.0", requiredMode = Schema.RequiredMode.REQUIRED)
    private String versionName;

    /**
     * 版本号，如 1
     */
    @Schema(description = "版本号", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer versionCode;

    /**
     * 平台：android、ios、all
     */
    @Schema(description = "平台", example = "android", allowableValues = {"android", "ios", "all"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String platform;

    /**
     * 发布日期
     */
    @Schema(description = "发布日期", example = "2025-10-27", requiredMode = Schema.RequiredMode.REQUIRED)
    private Date releaseDate;

    /**
     * 更新类型：major（重大更新）、minor（功能更新）、patch（修复更新）
     */
    @Schema(description = "更新类型", example = "minor", allowableValues = {"major", "minor", "patch"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private String updateType;

    /**
     * 更新日志，JSON数组格式
     * 说明：文件关联通过file_business_relation表管理，不在此表直接存储file_id
     */
    @Schema(description = "更新日志", example = "[\"新功能1\", \"修复bug1\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    private String changeLog;

    /**
     * 是否为最新版本：0-否，1-是
     */
    @Schema(description = "是否为最新版本", example = "1")
    private Boolean isLatest;

    /**
     * 下载次数
     */
    @Schema(description = "下载次数", example = "100")
    private Integer downloadCount;

    /**
     * 状态：published（已发布）、draft（草稿）、deprecated（已废弃）
     */
    @Schema(description = "状态", example = "published", allowableValues = {"published", "draft", "deprecated"})
    private String status;

    /**
     * 最低Android版本要求
     */
    @Schema(description = "最低Android版本要求", example = "5.0")
    private String minAndroidVersion;

    /**
     * 最低iOS版本要求
     */
    @Schema(description = "最低iOS版本要求", example = "11.0")
    private String minIosVersion;

    /**
     * 是否强制更新：0-否，1-是
     */
    @Schema(description = "是否强制更新", example = "false")
    private Boolean forceUpdate;

    /**
     * 备注信息
     */
    @Schema(description = "备注信息")
    private String remark;

    /**
     * 逻辑删除标记：0-未删除，1-已删除
     */
    @TableLogic
    @Schema(description = "删除标志", example = "0")
    private Boolean deleted;
}
