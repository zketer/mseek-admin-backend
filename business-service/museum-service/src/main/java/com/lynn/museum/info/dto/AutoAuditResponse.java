package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 自动审核响应
 *
 * @author lynn
 * @since 2024-12-16
 */
@Data
@Schema(description = "自动审核响应")
public class AutoAuditResponse {

    @Schema(description = "审核结果：1-通过，2-拒绝，3-异常标记")
    private Integer auditStatus;

    @Schema(description = "审核信心度（0-1之间，1表示最高信心度）")
    private Double confidence;

    @Schema(description = "审核备注/拒绝原因")
    private String auditRemark;

    @Schema(description = "异常类型：distance_anomaly, time_anomaly, frequency_anomaly, content_anomaly")
    private String anomalyType;

    @Schema(description = "风险等级：low, medium, high")
    private String riskLevel;

    @Schema(description = "是否需要人工复审")
    private Boolean needManualReview;

    /**
     * 创建审核通过的响应
     */
    public static AutoAuditResponse approved(String remark) {
        AutoAuditResponse response = new AutoAuditResponse();
        // 审核通过
        response.setAuditStatus(1);
        response.setConfidence(1.0);
        response.setAuditRemark(remark != null ? remark : "自动审核通过");
        response.setRiskLevel("low");
        response.setNeedManualReview(false);
        return response;
    }

    /**
     * 创建审核拒绝的响应
     */
    public static AutoAuditResponse rejected(String reason, String anomalyType) {
        AutoAuditResponse response = new AutoAuditResponse();
        // 审核拒绝
        response.setAuditStatus(2);
        response.setConfidence(0.8);
        response.setAuditRemark(reason != null ? reason : "自动审核拒绝");
        response.setAnomalyType(anomalyType);
        response.setRiskLevel("high");
        response.setNeedManualReview(true);
        return response;
    }

    /**
     * 创建异常标记的响应
     */
    public static AutoAuditResponse anomaly(String reason, String anomalyType) {
        AutoAuditResponse response = new AutoAuditResponse();
        // 异常标记
        response.setAuditStatus(3);
        response.setConfidence(0.6);
        response.setAuditRemark(reason != null ? reason : "检测到异常，需要人工审核");
        response.setAnomalyType(anomalyType);
        response.setRiskLevel("medium");
        response.setNeedManualReview(true);
        return response;
    }
}
