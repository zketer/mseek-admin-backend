package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 区县创建请求DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "区县创建请求")
public class DistrictCreateRequest {

    @NotBlank(message = "区域代码不能为空")
    @Schema(description = "区域代码", example = "110101")
    private String adcode;

    @NotBlank(message = "区县名称不能为空")
    @Schema(description = "区县名称", example = "东城区")
    private String name;

    @NotBlank(message = "所属城市代码不能为空")
    @Schema(description = "所属城市代码", example = "110100")
    private String cityAdcode;

    @Schema(description = "经度", example = "116.416357")
    private BigDecimal longitude;

    @Schema(description = "纬度", example = "39.928353")
    private BigDecimal latitude;
}
