package com.lynn.museum.system.dto;

import com.lynn.museum.common.entity.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 操作日志查询请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "操作日志查询请求")
public class OperationLogQueryRequest extends PageRequest {

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "操作用户名（模糊查询）")
    private String username;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作内容（模糊查询）")
    private String operation;

    @Schema(description = "操作IP")
    private String ip;

    @Schema(description = "操作状态：0-失败，1-成功")
    private Integer status;

    @Schema(description = "开始时间")
    private LocalDateTime startTime;

    @Schema(description = "结束时间")
    private LocalDateTime endTime;

}
