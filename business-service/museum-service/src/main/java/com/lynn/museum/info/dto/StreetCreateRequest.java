package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 街道创建请求DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "街道创建请求")
public class StreetCreateRequest {

    /**
     * 街道代码
     */
    @NotBlank(message = "街道代码不能为空")
    @Schema(description = "街道代码", requiredMode = Schema.RequiredMode.REQUIRED)
    private String adcode;

    /**
     * 街道名称
     */
    @NotBlank(message = "街道名称不能为空")
    @Schema(description = "街道名称", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    /**
     * 所属区县代码
     */
    @NotBlank(message = "所属区县代码不能为空")
    @Schema(description = "所属区县代码", requiredMode = Schema.RequiredMode.REQUIRED)
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
}
