package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 省份响应DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "省份响应")
public class ProvinceResponse {

    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "区域代码")
    private String adcode;

    @Schema(description = "省份名称")
    private String name;

    @Schema(description = "国家代码")
    private String countryAdcode;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
