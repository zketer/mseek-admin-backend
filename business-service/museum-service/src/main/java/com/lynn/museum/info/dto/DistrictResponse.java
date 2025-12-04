package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 区县响应DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "区县响应")
public class DistrictResponse {

    @Schema(description = "主键ID")
    private Integer id;

    @Schema(description = "区域代码")
    private String adcode;

    @Schema(description = "区县名称")
    private String name;

    @Schema(description = "所属城市代码")
    private String cityAdcode;

    @Schema(description = "经度")
    private BigDecimal longitude;

    @Schema(description = "纬度")
    private BigDecimal latitude;
}
