package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新分类请求DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "更新分类请求")
public class CategoryUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "分类ID")
    @NotNull(message = "分类ID不能为空")
    private Long id;

    @Schema(description = "分类名称")
    @NotBlank(message = "分类名称不能为空")
    @Size(max = 50, message = "分类名称长度不能超过50")
    private String name;

    @Schema(description = "分类描述")
    @Size(max = 200, message = "分类描述长度不能超过200")
    private String description;

    @Schema(description = "排序")
    private Integer sortOrder;

    @Schema(description = "状态：0-禁用，1-启用")
    @NotNull(message = "状态不能为空")
    private Integer status;
}
