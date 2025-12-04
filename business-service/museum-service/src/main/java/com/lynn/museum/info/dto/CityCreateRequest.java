package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 城市创建请求DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "城市创建请求")
public class CityCreateRequest {

    @NotBlank(message = "区域代码不能为空")
    @Schema(description = "区域代码", example = "110100")
    private String adcode;

    @NotBlank(message = "城市名称不能为空")
    @Schema(description = "城市名称", example = "北京市")
    private String name;

    @NotBlank(message = "所属省份代码不能为空")
    @Schema(description = "所属省份代码", example = "110000")
    private String provinceAdcode;

    @Schema(description = "城市编码", example = "010")
    private String citycode;

    @Schema(description = "经度", example = "116.407526")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "39.904030")
    private BigDecimal latitude;
}
