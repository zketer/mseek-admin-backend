package com.lynn.museum.info.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 博物馆响应DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "博物馆响应")
public class MuseumResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "博物馆ID")
    private Long id;

    @Schema(description = "博物馆名称")
    private String name;

    @Schema(description = "博物馆编码")
    private String code;

    @Schema(description = "博物馆描述")
    private String description;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "省份代码")
    private String provinceCode;

    @Schema(description = "省份名称")
    private String provinceName;

    @Schema(description = "城市代码")
    private String cityCode;

    @Schema(description = "城市名称")
    private String cityName;

    @Schema(description = "区县代码")
    private String districtCode;

    @Schema(description = "区县名称")
    private String districtName;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "官方网站")
    private String website;

    @Schema(description = "开放时间")
    private String openTime;

    @Schema(description = "门票价格（元）")
    private BigDecimal ticketPrice;

    @Schema(description = "门票说明")
    private String ticketDescription;

    @Schema(description = "日接待能力")
    private Integer capacity;

    @Schema(description = "状态：0-关闭，1-开放")
    private Integer status;

    @Schema(description = "等级：0-无等级，1-一级，2-二级，3-三级，4-四级，5-五级")
    private Integer level;

    @Schema(description = "博物馆类型")
    private String type;

    @Schema(description = "是否免费：0-收费，1-免费")
    private Integer freeAdmission;

    @Schema(description = "官方统计-藏品总数")
    private Integer collectionCount;

    @Schema(description = "官方统计-珍贵文物数量")
    private Integer preciousItems;

    @Schema(description = "官方统计-年度展览数量")
    private Integer exhibitions;

    @Schema(description = "官方统计-教育活动数量")
    private Integer educationActivities;

    @Schema(description = "官方统计-年度访客数（人次）")
    private Long visitorCount;

    @Schema(description = "分类列表")
    private List<CategoryInfo> categories;

    @Schema(description = "标签列表")
    private List<TagInfo> tags;

    @Schema(description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "图片文件ID列表")
    private List<Long> imageFileIds;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date createAt;

    @Schema(description = "更新时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date updateAt;

    @Schema(description = "距离用户的距离（格式：1.5km 或 500m）")
    private String distance;

    @Schema(description = "Logo文件ID")
    private Long logoFileId;

    @Schema(description = "Logo访问URL（动态生成）")
    private String logoUrl;

    /**
     * 分类信息
     */
    @Data
    @Schema(description = "分类信息")
    public static class CategoryInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "分类ID")
        private Long id;

        @Schema(description = "分类名称")
        private String name;

        @Schema(description = "分类编码")
        private String code;
    }

    /**
     * 标签信息
     */
    @Data
    @Schema(description = "标签信息")
    public static class TagInfo implements Serializable {
        private static final long serialVersionUID = 1L;

        @Schema(description = "标签ID")
        private Long id;

        @Schema(description = "标签名称")
        private String name;

        @Schema(description = "标签编码")
        private String code;

        @Schema(description = "标签颜色")
        private String color;
    }
}
