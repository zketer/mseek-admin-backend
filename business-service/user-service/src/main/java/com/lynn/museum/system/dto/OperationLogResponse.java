package com.lynn.museum.system.dto;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 操作日志响应
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "操作日志响应")
public class OperationLogResponse {

    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "操作用户ID")
    private Long userId;

    @Schema(description = "操作用户名")
    private String username;

    @Schema(description = "操作模块")
    private String module;

    @Schema(description = "操作内容")
    private String operation;

    @Schema(description = "请求方法")
    private String method;

    @Schema(description = "请求URL")
    private String requestUrl;

    @Schema(description = "请求参数")
    private String params;

    @Schema(description = "返回结果")
    private String result;

    @Schema(description = "操作IP")
    private String ip;

    @Schema(description = "操作地点")
    private String location;

    @Schema(description = "用户代理")
    private String userAgent;

    @Schema(description = "操作状态：0-失败，1-成功")
    private Integer status;

    @Schema(description = "错误信息")
    private String errorMsg;

    @Schema(description = "耗时(毫秒)")
    private Long costTime;

    @Schema(description = "创建时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createAt;

}
