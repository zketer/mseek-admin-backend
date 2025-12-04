package com.lynn.museum.common.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 分页请求基类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "分页请求")
public class PageRequest {

    @Schema(description = "页码，从1开始", example = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Long pageNum = 1L;

    @Schema(description = "每页大小", example = "10")
    @Min(value = 1, message = "每页大小不能小于1")
    @Max(value = 1000, message = "每页大小不能超过1000")
    private Long pageSize = 10L;

    @Schema(description = "排序字段")
    private String orderBy;

    @Schema(description = "排序方向：asc-升序，desc-降序")
    private String orderDirection = "desc";

}
