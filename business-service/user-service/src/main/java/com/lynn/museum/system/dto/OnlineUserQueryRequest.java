package com.lynn.museum.system.dto;

import com.lynn.museum.common.entity.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 在线用户查询请求
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "在线用户查询请求")
public class OnlineUserQueryRequest extends PageRequest {

    @Schema(description = "用户名（模糊查询）")
    private String username;

    @Schema(description = "昵称（模糊查询）")
    private String nickname;

    @Schema(description = "部门ID")
    private Long deptId;

    @Schema(description = "登录IP")
    private String ipAddr;

    @Schema(description = "会话状态：0-离线，1-在线")
    private Integer status;

}
