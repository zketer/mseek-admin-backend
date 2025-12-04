package com.lynn.museum.info.dto;

import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 展览响应DTO
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "展览响应")
public class ExhibitionResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    @Schema(description = "展览ID")
    private Long id;

    @Schema(description = "博物馆ID")
    private Long museumId;

    @Schema(description = "博物馆名称")
    private String museumName;

    @Schema(description = "展览标题")
    private String title;

    @Schema(description = "展览描述")
    private String description;

    @Schema(description = "封面图片URL")
    private String coverImage;

    @Schema(description = "开始日期")
    private LocalDate startDate;

    @Schema(description = "结束日期")
    private LocalDate endDate;

    @Schema(description = "展厅位置")
    private String location;

    @Schema(description = "门票价格")
    private BigDecimal ticketPrice;

    @Schema(description = "状态：0-已结束，1-进行中，2-未开始")
    private Integer status;

    @Schema(description = "是否常设展览：0-临时展览，1-常设展览")
    private Integer isPermanent;

    @Schema(description = "创建时间")
    private Date createAt;

    @Schema(description = "更新时间")
    private Date updateAt;

    @Schema(description = "图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "图片文件ID列表")
    private List<Long> imageFileIds;
}
