package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 城市响应DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "城市响应")
public class CityResponse {

    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "区域代码")
    private String adcode;

    @Schema(description = "城市名称")
    private String name;

    @Schema(description = "所属省份代码")
    private String provinceAdcode;

    @Schema(description = "所属省份名称")
    private String provinceName;

    @Schema(description = "城市编码")
    private String citycode;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
