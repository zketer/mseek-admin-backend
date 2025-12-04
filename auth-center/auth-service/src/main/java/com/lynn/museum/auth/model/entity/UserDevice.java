package com.lynn.museum.auth.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lynn.museum.common.entity.BaseEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 用户设备实体类
 * 用于APP端设备管理和长期登录
 * 
 * @author lynn
 * @since 2024-11-28
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("user_device")
@Schema(description = "用户设备")
public class UserDevice extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "设备唯一标识", example = "XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX")
    private String deviceId;

    @Schema(description = "设备名称", example = "iPhone 14 Pro")
    private String deviceName;

    @Schema(description = "设备型号", example = "iPhone15,2")
    private String deviceModel;

    @Schema(description = "操作系统版本", example = "iOS 17.0")
    private String osVersion;

    @Schema(description = "APP版本", example = "1.0.0")
    private String appVersion;

    @Schema(description = "平台类型：ios/android/web", example = "ios")
    private String platform;

    @Schema(description = "绑定的Refresh Token")
    private String refreshToken;

    @Schema(description = "最后活跃时间")
    private Date lastActiveTime;

    @Schema(description = "最后登录IP", example = "192.168.1.100")
    private String loginIp;

    @Schema(description = "最后登录地理位置", example = "北京市朝阳区")
    private String loginLocation;

    @Schema(description = "状态：0-禁用，1-正常", example = "1")
    private Integer status;
}
