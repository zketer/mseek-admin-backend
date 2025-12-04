package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 应用版本响应
 *
 * @author lynn
 * @since 2025-10-27
 */
@Data
@Schema(description = "应用版本响应")
public class AppVersionResponse {

    @Schema(description = "版本ID")
    private Long id;

    @Schema(description = "版本名称", example = "1.0.0")
    private String versionName;

    @Schema(description = "版本号", example = "1")
    private Integer versionCode;

    @Schema(description = "平台", example = "android")
    private String platform;

    @Schema(description = "发布日期", example = "2025-10-27")
    private Date releaseDate;

    @Schema(description = "更新类型", example = "minor")
    private String updateType;

    @Schema(description = "文件大小（易读格式）", example = "45.6 MB")
    private String fileSize;

    @Schema(description = "下载URL")
    private String downloadUrl;

    @Schema(description = "更新日志")
    private List<String> changeLog;

    @Schema(description = "是否为最新版本")
    private Boolean isLatest;

    @Schema(description = "下载次数")
    private Integer downloadCount;
    
    @Schema(description = "文件关系表ID")
    private Long fileRelationId;
    
    @Schema(description = "文件ID")
    private Long fileId;

    @Schema(description = "状态", example = "published")
    private String status;

    @Schema(description = "最低Android版本要求", example = "5.0")
    private String minAndroidVersion;

    @Schema(description = "最低iOS版本要求", example = "11.0")
    private String minIosVersion;

    @Schema(description = "是否强制更新")
    private Boolean forceUpdate;

    @Schema(description = "备注信息")
    private String remark;

    @Schema(description = "创建时间")
    private Date createAt;

    @Schema(description = "更新时间")
    private Date updateAt;
}

