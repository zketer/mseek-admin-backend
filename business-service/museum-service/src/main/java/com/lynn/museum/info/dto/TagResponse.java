package com.lynn.museum.info.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 标签响应DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "标签响应")
public class TagResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签ID")
    private Long id;

    @Schema(description = "标签名称")
    private String name;

    @Schema(description = "标签编码")
    private String code;

    @Schema(description = "标签描述")
    private String description;

    @Schema(description = "标签颜色")
    private String color;

    @Schema(description = "创建时间")
    private Date createAt;

    @Schema(description = "更新时间")
    private Date updateAt;
}
