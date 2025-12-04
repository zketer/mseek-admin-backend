package com.lynn.museum.info.model.entity;

import java.util.Date;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 打卡记录实体类
 *
 * @author lynn
 * @since 2024-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("checkin_record")
public class CheckinRecord extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 打卡记录ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 博物馆ID
     */
    private Long museumId;

    /**
     * 博物馆名称
     */
    private String museumName;

    /**
     * 打卡时间
     */
    private Date checkinTime;

    /**
     * 打卡纬度
     */
    private BigDecimal latitude;

    /**
     * 打卡经度
     */
    private BigDecimal longitude;

    /**
     * 打卡照片URL（旧字段，兼容）
     */
    private String photoUrl;

    /**
     * 打卡备注
     */
    private String remark;

    /**
     * 审核状态：0-待审核，1-审核通过，2-审核拒绝，3-异常标记
     */
    private Integer auditStatus;

    /**
     * 审核时间
     */
    private Date auditTime;

    /**
     * 审核人ID
     */
    private Long auditUserId;

    /**
     * 审核备注
     */
    private String auditRemark;

    /**
     * 异常类型：distance_anomaly, time_anomaly, frequency_anomaly
     */
    private String anomalyType;

    /**
     * 打卡照片列表（JSON格式存储多张照片）
     */
    private String photoUrls;

    /**
     * 照片URLs（新字段，JSON格式）
     */
    private String photos;

    /**
     * 打卡感受
     */
    private String feeling;

    /**
     * 评分（1-5）
     */
    private Integer rating;

    /**
     * 天气状况
     */
    private String weather;

    /**
     * 同行伙伴（JSON格式）
     */
    private String companions;

    /**
     * 标签（JSON格式）
     */
    private String tags;

    /**
     * 地址描述
     */
    private String address;

    /**
     * 是否为暂存记录
     */
    private Boolean isDraft;

    /**
     * 暂存ID（前端生成）
     */
    private String draftId;

    /**
     * 心情状态：happy, excited, peaceful, inspired, grateful
     */
    private String mood;

    /**
     * 设备信息（JSON格式）
     */
    private String deviceInfo;
}
