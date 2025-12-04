package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 街道响应DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "街道响应信息")
public class StreetResponse {

    /**
     * 主键ID
     */
    @Schema(description = "主键ID")
    private Integer id;

    /**
     * 街道代码
     */
    @Schema(description = "街道代码")
    private String adcode;

    /**
     * 街道名称
     */
    @Schema(description = "街道名称")
    private String name;

    /**
     * 所属区县代码
     */
    @Schema(description = "所属区县代码")
    private String districtAdcode;

    /**
     * 经度
     */
    @Schema(description = "经度")
    private BigDecimal longitude;

    /**
     * 纬度
     */
    @Schema(description = "纬度")
    private BigDecimal latitude;

    /**
     * 所属区县名称
     */
    @Schema(description = "所属区县名称")
    private String districtName;

    /**
     * 所属城市名称
     */
    @Schema(description = "所属城市名称")
    private String cityName;

    /**
     * 所属城市代码
     */
    @Schema(description = "所属城市代码")
    private String cityAdcode;

    /**
     * 所属省份名称
     */
    @Schema(description = "所属省份名称")
    private String provinceName;

    /**
     * 所属省份代码
     */
    @Schema(description = "所属省份代码")
    private String provinceAdcode;
}
