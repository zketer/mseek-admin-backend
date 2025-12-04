package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

/**
 * 区域行政区划响应DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "区域行政区划响应")
public class AreaDivisionResponse {

    @Schema(description = "ID")
    private Integer id;

    @Schema(description = "区域代码")
    private String adcode;

    @Schema(description = "名称")
    private String name;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;

    @Schema(description = "层级：province/city/district")
    private String level;

    @Schema(description = "父级代码")
    private String parentCode;

    @Schema(description = "城市编码（仅城市级别有）")
    private String citycode;

    @Schema(description = "子级区域列表")
    private List<AreaDivisionResponse> children;

    @Schema(description = "所属城市名称")
    private String cityName;

    @Schema(description = "所属城市代码")
    private String cityAdcode;

    @Schema(description = "所属省份名称")
    private String provinceName;

    @Schema(description = "所属省份代码")
    private String provinceAdcode;
}
