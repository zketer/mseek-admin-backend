package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import java.util.Date;

/**
 * 公告查询请求
 */
@Data
@Schema(description = "公告查询请求")
public class AnnouncementQueryRequest {

    @Schema(description = "当前页码", example = "1")
    private Integer current = 1;

    @Schema(description = "页面大小", example = "10")
    private Integer size = 10;

    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告类型")
    private String type;

    @Schema(description = "优先级：0-普通，1-重要，2-紧急")
    @Min(0)
    private Integer priority;

    @Schema(description = "状态：0-草稿，1-发布，2-下线")
    @Min(0)
    private Integer status;

    @Schema(description = "开始时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    @Schema(description = "结束时间")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
