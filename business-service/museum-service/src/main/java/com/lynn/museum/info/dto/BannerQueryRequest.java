package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 轮播图查询请求
 *
 * @author lynn
 * @since 2024-12-16
 */
@Data
@Schema(description = "轮播图查询请求")
public class BannerQueryRequest {

    @Schema(description = "当前页码", example = "1")
    private Integer current = 1;

    @Schema(description = "页面大小", example = "10")
    private Integer size = 10;

    @Schema(description = "标题")
    private String title;

    @Schema(description = "链接类型：museum/exhibition/external/none")
    private String linkType;

    @Schema(description = "状态：0-下线，1-上线")
    private Integer status;
}
