package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Date;

/**
 * 轮播图创建请求
 *
 * @author lynn
 * @since 2024-12-16
 */
@Data
@Schema(description = "轮播图创建请求")
public class BannerCreateRequest {

    @Schema(description = "轮播图标题", example = "故宫博物院推荐")
    @NotBlank(message = "标题不能为空")
    private String title;

    @Schema(description = "图片URL", example = "https://example.com/banner1.jpg")
    @NotBlank(message = "图片URL不能为空")
    private String imageUrl;

    @Schema(description = "链接类型：museum/exhibition/external/none", example = "museum")
    private String linkType;

    @Schema(description = "链接值", example = "1")
    private String linkValue;

    @Schema(description = "排序权重", example = "1")
    @NotNull(message = "排序权重不能为空")
    private Integer sort;

    @Schema(description = "状态：0-下线，1-上线", example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;

    @Schema(description = "文件ID（上传的图片文件ID）", example = "123")
    private Long fileId;
}
