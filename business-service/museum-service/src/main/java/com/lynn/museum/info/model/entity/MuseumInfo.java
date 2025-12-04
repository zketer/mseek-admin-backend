package com.lynn.museum.info.model.entity;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 博物馆信息实体类
 *
 * @author lynn
 * @since 2024-01-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("museum_info")
public class MuseumInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 博物馆ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    /**
     * 博物馆名称
     */
    private String name;

    /**
     * 博物馆编码
     */
    private String code;

    /**
     * 博物馆描述
     */
    private String description;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 省份代码
     */
    private String provinceCode;

    /**
     * 城市代码
     */
    private String cityCode;

    /**
     * 区县代码
     */
    private String districtCode;

    /**
     * 经度
     */
    private BigDecimal longitude;

    /**
     * 纬度
     */
    private BigDecimal latitude;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 官方网站
     */
    private String website;

    /**
     * 开放时间
     */
    private String openTime;

    /**
     * 门票价格（元）
     */
    private BigDecimal ticketPrice;

    /**
     * 门票说明
     */
    private String ticketDescription;

    /**
     * 日接待能力
     */
    private Integer capacity;

    /**
     * 状态：0-关闭，1-开放
     */
    private Integer status;

    /**
     * 等级：0-无等级，1-一级，2-二级，3-三级，4-四级，5-五级
     */
    private Integer level;

    /**
     * 博物馆类型
     */
    private String type;

    /**
     * 是否免费：0-收费，1-免费
     */
    private Integer freeAdmission;

    /**
     * 官方统计-藏品总数
     */
    private Integer collectionCount;

    /**
     * 官方统计-珍贵文物数量
     */
    private Integer preciousItems;

    /**
     * 官方统计-年度展览数量
     */
    private Integer exhibitions;

    /**
     * 官方统计-教育活动数量
     */
    private Integer educationActivities;

    /**
     * 官方统计-年度访客数（人次）
     */
    private Long visitorCount;

    /**
     * 是否展示：0-不展示，1-展示
     */
    private Integer display;
}
