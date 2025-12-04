package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;

/**
 * 更新标签请求DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "更新标签请求")
public class TagUpdateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "标签ID")
    @NotNull(message = "标签ID不能为空")
    private Long id;

    @Schema(description = "标签名称")
    @NotBlank(message = "标签名称不能为空")
    @Size(max = 50, message = "标签名称长度不能超过50")
    private String name;

    @Schema(description = "标签描述")
    @Size(max = 200, message = "标签描述长度不能超过200")
    private String description;

    @Schema(description = "标签颜色")
    @Size(max = 20, message = "标签颜色长度不能超过20")
    private String color;
}
