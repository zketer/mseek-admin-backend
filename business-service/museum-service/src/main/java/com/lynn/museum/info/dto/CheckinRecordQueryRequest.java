package com.lynn.museum.info.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 打卡记录查询请求
 *
 * @author lynn
 * @since 2024-12-16
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(description = "打卡记录查询请求")
public class CheckinRecordQueryRequest {

    @Schema(description = "当前页码", example = "1")
    private Integer current = 1;

    @Schema(description = "页面大小", example = "10")
    private Integer size = 10;

    @Schema(description = "页码（兼容小程序）", example = "1")
    private Integer page = 1;

    @Schema(description = "页大小（兼容小程序）", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String userName;

    @Schema(description = "博物馆ID")
    private Long museumId;

    @Schema(description = "博物馆名称")
    private String museumName;

    @Schema(description = "审核状态：0-待审核，1-审核通过，2-审核拒绝，3-异常标记")
    private Integer auditStatus;

    @Schema(description = "异常类型")
    private String anomalyType;

    @Schema(description = "开始时间")
    private Date startTime;

    @Schema(description = "结束时间")
    private Date endTime;

    @Schema(description = "是否只查询异常记录")
    private Boolean anomalyOnly;

    @Schema(description = "是否为暂存记录")
    private Boolean isDraft;

    @Schema(description = "开始日期（字符串格式）", example = "2024-01-01")
    private String startDate;

    @Schema(description = "结束日期（字符串格式）", example = "2024-01-31")
    private String endDate;

    @Schema(description = "搜索关键词（博物馆名称、省市）", example = "故宫")
    private String keyword;

    @Schema(description = "筛选类型", example = "all", allowableValues = {"all", "thisMonth", "thisYear"})
    private String filterType;
}
