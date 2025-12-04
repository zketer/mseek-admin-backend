package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 博物馆查询请求DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "博物馆查询请求")
public class MuseumQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "博物馆名称（模糊搜索）")
    private String name;

    @Schema(description = "省份代码")
    private String provinceCode;

    @Schema(description = "城市代码")
    private String cityCode;

    @Schema(description = "区县代码")
    private String districtCode;

    @Schema(description = "街道代码")
    private String streetCode;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "标签ID")
    private Long tagId;

    @Schema(description = "状态：0-关闭，1-开放")
    private Integer status;
    
    @Schema(description = "等级：0-无等级，1-一级，2-二级，3-三级，4-四级，5-五级")
    private Integer level;

    @Schema(description = "博物馆类型")
    private String type;

    @Schema(description = "是否免费：0-收费，1-免费")
    private Integer freeAdmission;

    @Schema(description = "最小藏品数量")
    private Integer minCollectionCount;

    @Schema(description = "最大藏品数量")
    private Integer maxCollectionCount;

    @Schema(description = "排序方式：hot-人气最高，collection-藏品最多")
    private String sortBy;
}
