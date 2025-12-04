package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 街道更新请求DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "街道更新请求")
public class StreetUpdateRequest {

    /**
     * 街道名称
     */
    @Schema(description = "街道名称")
    private String name;

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
