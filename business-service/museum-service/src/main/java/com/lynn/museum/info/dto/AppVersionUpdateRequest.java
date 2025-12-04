package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 应用版本更新请求
 *
 * @author lynn
 * @since 2025-10-27
 */
@Data
@Schema(description = "应用版本更新请求")
public class AppVersionUpdateRequest {

    @Schema(description = "版本名称", example = "1.0.0")
    private String versionName;

    @Schema(description = "版本号", example = "1")
    private Integer versionCode;

    @Schema(description = "平台", example = "android", allowableValues = {"android", "ios", "all"})
    private String platform;

    @Schema(description = "发布日期", example = "2025-10-27")
    private Date releaseDate;

    @Schema(description = "更新类型", example = "minor", allowableValues = {"major", "minor", "patch"})
    private String updateType;

    @Schema(description = "关联文件ID（APK/IPA文件）", example = "1")
    private Long fileId;
    
    @Schema(description = "文件关联表ID", example = "1")
    private Long fileRelationId;
    
    @Schema(description = "更新者ID")
    private Long updateBy;

    @Schema(description = "更新日志", example = "[\"新功能1\", \"修复bug1\"]")
    private List<String> changeLog;

    @Schema(description = "是否为最新版本", example = "false")
    private Boolean isLatest;

    @Schema(description = "状态", example = "published", allowableValues = {"published", "draft", "deprecated"})
    private String status;

    @Schema(description = "最低Android版本要求", example = "5.0")
    private String minAndroidVersion;

    @Schema(description = "最低iOS版本要求", example = "11.0")
    private String minIosVersion;

    @Schema(description = "是否强制更新", example = "false")
    private Boolean forceUpdate;

    @Schema(description = "备注信息")
    private String remark;
}

