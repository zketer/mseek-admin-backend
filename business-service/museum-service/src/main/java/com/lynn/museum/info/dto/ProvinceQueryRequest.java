package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 省份查询请求DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "省份查询请求")
public class ProvinceQueryRequest {

    @Schema(description = "当前页", example = "1")
    private Integer current = 1;

    @Schema(description = "页面大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "关键词搜索（省份名称）", example = "北京")
    private String keyword;

    @Schema(description = "区域代码", example = "110000")
    private String adcode;

    @Schema(description = "国家代码", example = "100000")
    private String countryAdcode;
}
