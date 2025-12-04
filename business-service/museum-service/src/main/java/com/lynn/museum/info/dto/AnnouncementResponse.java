package com.lynn.museum.info.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 公告响应
 */
@Data
@Schema(description = "公告响应")
public class AnnouncementResponse {

    @Schema(description = "公告ID")
    private Long id;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "公告类型")
    private String type;

    @Schema(description = "优先级：0-普通，1-重要，2-紧急")
    private Integer priority;

    @Schema(description = "状态：0-草稿，1-发布，2-下线")
    private Integer status;

    @Schema(description = "启用状态：0-禁用（隐藏），1-启用（显示）")
    private Integer enabled;

    @Schema(description = "发布时间（时间戳）")
    private Long publishTime;

    @Schema(description = "过期时间（时间戳）")
    private Long expireTime;

    @Schema(description = "阅读次数")
    private Integer readCount;

    @Schema(description = "创建人")
    private String createBy;

    @Schema(description = "创建时间")
    private Date createAt;

    @Schema(description = "更新时间")
    private Date updateAt;
}
