package com.lynn.museum.system.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户统计信息响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户统计信息响应")
public class UserStatisticsResponse {

    @Schema(description = "总用户数")
    private Long totalUsers;

    @Schema(description = "活跃用户数")
    private Long activeUsers;

    @Schema(description = "非活跃用户数")
    private Long inactiveUsers;

    @Schema(description = "锁定用户数")
    private Long lockedUsers;

    @Schema(description = "用户增长趋势")
    private List<UserGrowthTrend> userGrowthTrend;

    @Schema(description = "角色分布")
    private List<RoleDistribution> roleDistribution;

    @Schema(description = "总角色数")
    private Long totalRoles;

    @Schema(description = "权限分布")
    private List<PermissionDistribution> permissionDistribution;

    @Schema(description = "总权限数")
    private Long totalPermissions;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "用户增长趋势")
    public static class UserGrowthTrend {
        @Schema(description = "日期")
        private String date;

        @Schema(description = "用户数量")
        private Long count;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "角色分布")
    public static class RoleDistribution {
        @Schema(description = "角色名称")
        private String type;

        @Schema(description = "用户数量")
        private Long value;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "权限分布")
    public static class PermissionDistribution {
        @Schema(description = "权限类型")
        private String type;

        @Schema(description = "权限数量")
        private Long value;
    }
}
