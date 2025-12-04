package com.lynn.museum.system.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 在线用户响应
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@Schema(description = "在线用户响应")
public class OnlineUserResponse {

    @Schema(description = "会话ID")
    private String sessionId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "用户名")
    private String username;

    @Schema(description = "昵称")
    private String nickname;

    @Schema(description = "部门名称")
    private String deptName;

    @Schema(description = "登录IP")
    private String ipAddr;

    @Schema(description = "登录地点")
    private String loginLocation;

    @Schema(description = "浏览器")
    private String browser;

    @Schema(description = "操作系统")
    private String os;

    @Schema(description = "登录时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime loginTime;

    @Schema(description = "最后访问时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastAccessTime;

    @Schema(description = "会话状态：0-离线，1-在线")
    private Integer status;

}
