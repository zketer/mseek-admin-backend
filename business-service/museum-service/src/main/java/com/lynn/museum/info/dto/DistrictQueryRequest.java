package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 区县查询请求DTO
 *
 * @author lynn
 */
@Data
@Schema(description = "区县查询请求")
public class DistrictQueryRequest {

    @Schema(description = "当前页", example = "1")
    private Integer current = 1;

    @Schema(description = "页面大小", example = "20")
    private Integer pageSize = 20;

    @Schema(description = "关键词搜索（区县名称）", example = "东城")
    private String keyword;

    @Schema(description = "区域代码", example = "110101")
    private String adcode;

    @Schema(description = "所属城市代码", example = "110100")
    private String cityAdcode;
}
