package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

/**
 * 展览查询请求DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "展览查询请求")
public class ExhibitionQueryRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "博物馆ID")
    private Long museumId;

    @Schema(description = "页码", example = "1")
    private Integer page = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer size = 10;

    @Schema(description = "展览标题（模糊搜索）")
    private String title;

    @Schema(description = "状态：0-已结束，1-进行中，2-未开始")
    private Integer status;

    @Schema(description = "是否常设展览：0-临时展览，1-常设展览")
    private Integer isPermanent;
}
