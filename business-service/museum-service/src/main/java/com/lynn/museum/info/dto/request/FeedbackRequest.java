package com.lynn.museum.info.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 反馈建议请求DTO
 */
@Data
@Schema(description = "反馈建议请求")
public class FeedbackRequest {

    @NotBlank(message = "收件人不能为空")
    @Schema(description = "收件人邮箱", example = "museumseek@163.com")
    private String to;

    @NotBlank(message = "邮件主题不能为空")
    @Schema(description = "邮件主题", example = "【文博探索】功能建议 - 用户反馈")
    private String subject;

    @NotBlank(message = "反馈类型不能为空")
    @Schema(description = "反馈类型", example = "feature")
    private String type;

    @NotBlank(message = "反馈类型名称不能为空")
    @Schema(description = "反馈类型名称", example = "功能建议")
    private String typeName;

    @NotBlank(message = "反馈内容不能为空")
    @Size(min = 10, max = 500, message = "反馈内容长度必须在10-500个字符之间")
    @Schema(description = "反馈内容")
    private String content;

    @Schema(description = "联系方式", example = "user@example.com")
    private String contact;

    @Schema(description = "提交时间", example = "2024-01-01 12:00:00")
    private String timestamp;

    @Schema(description = "用户环境", example = "WeChat MiniProgram - 文博探索")
    private String userAgent;
}
