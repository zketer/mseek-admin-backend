package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 打卡审核请求
 *
 * @author lynn
 * @since 2024-12-16
 */
@Data
@Schema(description = "打卡审核请求")
public class CheckinAuditRequest {

    @Schema(description = "审核状态：1-审核通过，2-审核拒绝，3-异常标记", example = "1")
    @NotNull(message = "审核状态不能为空")
    private Integer auditStatus;

    @Schema(description = "审核备注", example = "照片清晰，位置准确")
    private String auditRemark;
}
