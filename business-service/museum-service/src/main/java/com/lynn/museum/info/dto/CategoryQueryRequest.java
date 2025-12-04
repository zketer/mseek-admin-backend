package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类查询请求DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "分类查询请求")
public class CategoryQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "分类名称（模糊搜索）")
    private String name;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;
}
