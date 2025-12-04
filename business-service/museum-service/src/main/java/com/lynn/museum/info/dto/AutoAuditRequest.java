package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 自动审核请求
 *
 * @author lynn
 * @since 2024-12-16
 */
@Data
@Schema(description = "自动审核请求")
public class AutoAuditRequest {

    @Schema(description = "打卡记录ID")
    private Long checkinId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "博物馆ID")
    private Long museumId;

    @Schema(description = "打卡时间")
    private Date checkinTime;

    @Schema(description = "打卡纬度")
    private BigDecimal latitude;

    @Schema(description = "打卡经度")
    private BigDecimal longitude;

    @Schema(description = "打卡照片URL列表（JSON格式）")
    private String photoUrls;

    @Schema(description = "打卡备注")
    private String remark;

    @Schema(description = "心情状态")
    private String mood;

    @Schema(description = "设备信息（JSON格式）")
    private String deviceInfo;
}
