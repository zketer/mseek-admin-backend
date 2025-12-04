package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Date;

/**
 * 公告创建请求
 */
@Data
@Schema(description = "公告创建请求")
public class AnnouncementCreateRequest {

    @Schema(description = "公告标题", example = "系统维护公告")
    @NotBlank(message = "公告标题不能为空")
    private String title;

    @Schema(description = "公告内容")
    @NotBlank(message = "公告内容不能为空")
    private String content;

    @Schema(description = "公告类型：general/maintenance/activity", example = "general")
    @NotBlank(message = "公告类型不能为空")
    private String type;

    @Schema(description = "优先级：0-普通，1-重要，2-紧急", example = "0")
    @NotNull(message = "优先级不能为空")
    private Integer priority;

    @Schema(description = "状态：0-草稿，1-发布，2-下线", example = "1")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "启用状态：0-禁用（隐藏），1-启用（显示）", example = "1")
    private Integer enabled;

    @Schema(description = "发布时间（时间戳）")
    private Long publishTime;

    @Schema(description = "过期时间（时间戳）")
    private Long expireTime;
}
