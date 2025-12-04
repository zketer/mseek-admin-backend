package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 应用版本创建请求
 *
 * @author lynn
 * @since 2025-10-27
 */
@Data
@Schema(description = "应用版本创建请求")
public class AppVersionCreateRequest {

    @Schema(description = "版本名称", example = "1.0.0", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "版本名称不能为空")
    private String versionName;

    @Schema(description = "版本号", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "版本号不能为空")
    private Integer versionCode;

    @Schema(description = "平台", example = "android", allowableValues = {"android", "ios", "all"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "平台不能为空")
    private String platform;

    @Schema(description = "发布日期", example = "2025-10-27", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "发布日期不能为空")
    private Date releaseDate;

    @Schema(description = "更新类型", example = "minor", allowableValues = {"major", "minor", "patch"}, requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "更新类型不能为空")
    private String updateType;

    @Schema(description = "关联文件ID（APK/IPA文件）", example = "1")
    @NotNull(message = "文件ID不能为空")
    private Long fileId;

    @Schema(description = "更新日志", example = "[\"新功能1\", \"修复bug1\"]", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotNull(message = "更新日志不能为空")
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

