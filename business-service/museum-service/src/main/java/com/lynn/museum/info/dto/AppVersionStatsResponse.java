package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 应用版本统计响应
 *
 * @author lynn
 * @since 2025-10-27
 */
@Data
@Schema(description = "应用版本统计响应")
public class AppVersionStatsResponse {

    @Schema(description = "版本总数")
    private Integer totalVersions;

    @Schema(description = "最新版本号")
    private String latestVersion;

    @Schema(description = "总下载量")
    private Long totalDownloads;

    @Schema(description = "Android版本数")
    private Integer androidVersions;

    @Schema(description = "iOS版本数")
    private Integer iosVersions;
}

