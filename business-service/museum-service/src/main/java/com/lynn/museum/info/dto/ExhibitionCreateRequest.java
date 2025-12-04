package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 创建展览请求DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "创建展览请求")
public class ExhibitionCreateRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "博物馆ID")
    @NotNull(message = "博物馆ID不能为空")
    private Long museumId;

    @Schema(description = "展览标题")
    @NotBlank(message = "展览标题不能为空")
    @Size(max = 100, message = "展览标题长度不能超过100")
    private String title;

    @Schema(description = "展览描述")
    private String description;

    @Schema(description = "封面图片URL")
    @Size(max = 500, message = "封面图片URL长度不能超过500")
    private String coverImage;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "展厅位置")
    @Size(max = 100, message = "展厅位置长度不能超过100")
    private String location;

    @Schema(description = "门票价格")
    private BigDecimal ticketPrice;

    @Schema(description = "状态：0-已结束，1-进行中，2-未开始")
    @NotNull(message = "状态不能为空")
    private Integer status;

    @Schema(description = "是否常设展览：0-临时展览，1-常设展览")
    @NotNull(message = "是否常设展览不能为空")
    private Integer isPermanent;

    @Schema(description = "图片文件ID列表")
    private List<Long> fileIds;
}
