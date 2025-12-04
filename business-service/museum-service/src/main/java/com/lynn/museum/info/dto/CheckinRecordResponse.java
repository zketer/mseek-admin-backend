package com.lynn.museum.info.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 打卡记录响应
 *
 * @author lynn
 * @since 2024-12-16
 */
@Data
@Schema(description = "打卡记录响应")
public class CheckinRecordResponse {

    @Schema(description = "打卡记录ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "用户昵称")
    private String userNickname;

    @Schema(description = "博物馆ID")
    private Long museumId;

    @Schema(description = "博物馆名称")
    private String museumName;

    @Schema(description = "博物馆地址")
    private String museumAddress;

    @Schema(description = "打卡时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date checkinTime;

    @Schema(description = "打卡纬度")
    private BigDecimal latitude;

    @Schema(description = "打卡经度")
    private BigDecimal longitude;

    @Schema(description = "打卡备注")
    private String remark;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝，3-异常标记")
    private Integer auditStatus;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date auditTime;

    @Schema(description = "审核人ID")
    private Long auditUserId;

    @Schema(description = "审核人姓名")
    private String auditUserName;

    @Schema(description = "审核备注")
    private String auditRemark;

    @Schema(description = "异常类型")
    private String anomalyType;

    @Schema(description = "打卡照片列表（JSON字符串）")
    private String photoUrls;

    @Schema(description = "照片列表（JSON字符串）")
    private String photos;

    @Schema(description = "打卡感受")
    private String feeling;

    @Schema(description = "评分（1-5）")
    private Integer rating;

    @Schema(description = "心情状态")
    private String mood;

    @Schema(description = "天气状况")
    private String weather;

    @Schema(description = "同行伙伴（JSON字符串）")
    private String companions;

    @Schema(description = "标签（JSON字符串）")
    private String tags;

    @Schema(description = "地址描述")
    private String address;

    @Schema(description = "设备信息")
    private String deviceInfo;

    @Schema(description = "距离博物馆距离（米）")
    private Double distance;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createAt;
}
