package com.lynn.museum.info.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 分类响应DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "分类响应")
public class CategoryResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID")
    private Long id;

    @Schema(description = "分类名称")
    private String name;

    @Schema(description = "分类编码")
    private String code;

    @Schema(description = "分类描述")
    private String description;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    private Integer status;

    @Schema(description = "创建时间")
    private Date createAt;

    @Schema(description = "更新时间")
    private Date updateAt;
}
