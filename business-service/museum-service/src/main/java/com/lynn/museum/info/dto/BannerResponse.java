package com.lynn.museum.info.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 轮播图响应
 *
 * @author lynn
 * @since 2024-12-16
 */
@Data
@Schema(description = "轮播图响应")
public class BannerResponse {

    @Schema(description = "轮播图ID")
    private Long id;

    @Schema(description = "轮播图标题")
    private String title;

    @Schema(description = "图片URL")
    private String imageUrl;

    @Schema(description = "链接类型：museum/exhibition/external/none")
    private String linkType;

    @Schema(description = "链接值")
    private String linkValue;

    @Schema(description = "排序权重")
    private Integer sort;

    @Schema(description = "状态：0-下线，1-上线")
    private Integer status;

    @Schema(description = "开始时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @Schema(description = "结束时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    @Schema(description = "点击次数")
    private Integer clickCount;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateAt;

    @Schema(description = "创建人")
    private Long createBy;

    @Schema(description = "更新人")
    private Long updateBy;

    @Schema(description = "文件ID（关联的图片文件ID）")
    private Long fileId;
}
