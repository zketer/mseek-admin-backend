package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 创建博物馆请求DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "创建博物馆请求")
public class MuseumCreateRequest implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "博物馆名称")
    @NotBlank(message = "博物馆名称不能为空")
    @Size(max = 100, message = "博物馆名称长度不能超过100")
    private String name;

    @Schema(description = "博物馆编码")
    @NotBlank(message = "博物馆编码不能为空")
    @Size(max = 50, message = "博物馆编码长度不能超过50")
    private String code;

    @Schema(description = "博物馆描述")
    private String description;

    @Schema(description = "详细地址")
    @Size(max = 200, message = "详细地址长度不能超过200")
    private String address;

    @Schema(description = "省份代码")
    @Size(max = 10, message = "省份代码长度不能超过10")
    private String provinceCode;

    @Schema(description = "城市代码")
    @Size(max = 10, message = "城市代码长度不能超过10")
    private String cityCode;

    @Schema(description = "区县代码")
    @Size(max = 10, message = "区县代码长度不能超过10")
    private String districtCode;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "联系电话")
    @Size(max = 20, message = "联系电话长度不能超过20")
    private String phone;

    @Schema(description = "官方网站")
    @Size(max = 200, message = "官方网站长度不能超过200")
    private String website;

    @Schema(description = "开放时间")
    @Size(max = 200, message = "开放时间长度不能超过200")
    private String openTime;

    @Schema(description = "门票价格（元）")
    private BigDecimal ticketPrice;

    @Schema(description = "门票说明")
    @Size(max = 500, message = "门票说明长度不能超过500")
    private String ticketDescription;

    @Schema(description = "日接待能力")
    private Integer capacity;

    @Schema(description = "状态：0-关闭，1-开放")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "等级：0-无等级，1-一级，2-二级，3-三级，4-四级，5-五级")
    @NotNull(message = "等级不能为空")
    private Integer level;

    @Schema(description = "博物馆类型")
    @Size(max = 100, message = "博物馆类型长度不能超过100")
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

    @Schema(description = "分类ID列表")
    private List<Long> categoryIds;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Schema(description = "文件ID列表（博物馆图片）")
    private List<Long> fileIds;

    @Schema(description = "Logo文件ID")
    private Long logoFileId;
}
