package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 应用版本查询请求
 *
 * @author lynn
 * @since 2025-10-27
 */
@Data
@Schema(description = "应用版本查询请求")
public class AppVersionQueryRequest {

    @Schema(description = "当前页", example = "1")
    private Integer current = 1;

    @Schema(description = "每页数量", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "搜索关键词（版本名称或更新日志）")
    private String keyword;

    @Schema(description = "平台筛选", example = "android", allowableValues = {"android", "ios", "all"})
    private String platform;

    @Schema(description = "状态筛选", example = "published", allowableValues = {"published", "draft", "deprecated"})
    private String status;
}

