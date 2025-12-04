package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 省份创建请求DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "省份创建请求")
public class ProvinceCreateRequest {

    @NotBlank(message = "区域代码不能为空")
    @Schema(description = "区域代码", example = "110000")
    private String adcode;

    @NotBlank(message = "省份名称不能为空")
    @Schema(description = "省份名称", example = "北京市")
    private String name;

    @NotBlank(message = "国家代码不能为空")
    @Schema(description = "国家代码", example = "100000")
    private String countryAdcode;

    @Schema(description = "经度", example = "116.407526")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "39.904030")
    private BigDecimal latitude;
}
