package com.lynn.museum.system.service.impl;

import com.lynn.museum.system.dto.UserQueryRequest;
import com.lynn.museum.system.dto.UserStatisticsResponse;
import com.lynn.museum.system.mapper.PermissionMapper;
import com.lynn.museum.system.mapper.RoleMapper;
import com.lynn.museum.system.mapper.UserMapper;
import com.lynn.museum.system.mapper.UserRoleMapper;
import com.lynn.museum.system.model.entity.Permission;
import com.lynn.museum.system.model.entity.Role;
import com.lynn.museum.system.service.UserStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户统计信息服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserStatisticsServiceImpl implements UserStatisticsService {

    private final UserMapper userMapper;
    private final RoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public UserStatisticsResponse getUserStatistics(Integer days) {
        // 获取总用户数
        UserQueryRequest query = new UserQueryRequest();
        query.setDeleted(0);
        Long totalUsers = userMapper.selectCount(query);

        // 获取活跃用户数（暂时使用启用状态用户数量，后续可通过认证服务获取真实活跃数据）
        UserQueryRequest activeQuery = new UserQueryRequest();
        activeQuery.setDeleted(0);
        activeQuery.setStatus(1);
        Long activeUsers = userMapper.selectCount(activeQuery);

        // 获取锁定用户数
        UserQueryRequest lockedQuery = new UserQueryRequest();
        lockedQuery.setDeleted(0);
        lockedQuery.setStatus(0);
        Long lockedUsers = userMapper.selectCount(lockedQuery);

        // 计算非活跃用户数
        Long inactiveUsers = totalUsers - activeUsers - lockedUsers;

        // 获取用户增长趋势
        List<UserStatisticsResponse.UserGrowthTrend> userGrowthTrend = getUserGrowthTrend(days);

        // 获取角色分布
        List<Role> roles = roleMapper.selectAll();
        Long totalRoles = (long) roles.size();

        // 获取角色分布（使用真实数据）
        List<Map<String, Object>> roleDistributionData = userRoleMapper.countRoleDistribution();
        List<UserStatisticsResponse.RoleDistribution> roleDistribution = roleDistributionData.stream()
                .map(item -> new UserStatisticsResponse.RoleDistribution(
                        String.valueOf(item.get("type")),
                        Long.valueOf(String.valueOf(item.get("value")))))
                .collect(Collectors.toList());

        // 获取权限分布
        List<Permission> permissions = permissionMapper.selectAll();
        Long totalPermissions = (long) permissions.size();

        // 按权限类型分组统计
        Map<Integer, Long> permissionTypeCountMap = permissions.stream()
                .collect(Collectors.groupingBy(Permission::getPermissionType, Collectors.counting()));

        List<UserStatisticsResponse.PermissionDistribution> permissionDistribution = new ArrayList<>();
        permissionDistribution.add(new UserStatisticsResponse.PermissionDistribution(
                "菜单权限", permissionTypeCountMap.getOrDefault(1, 0L)));
        permissionDistribution.add(new UserStatisticsResponse.PermissionDistribution(
                "按钮权限", permissionTypeCountMap.getOrDefault(2, 0L)));
        permissionDistribution.add(new UserStatisticsResponse.PermissionDistribution(
                "接口权限", permissionTypeCountMap.getOrDefault(3, 0L)));

        // 构建并返回响应
        return UserStatisticsResponse.builder()
                .totalUsers(totalUsers)
                .activeUsers(activeUsers)
                .inactiveUsers(inactiveUsers)
                .lockedUsers(lockedUsers)
                .userGrowthTrend(userGrowthTrend)
                .totalRoles(totalRoles)
                .roleDistribution(roleDistribution)
                .totalPermissions(totalPermissions)
                .permissionDistribution(permissionDistribution)
                .build();
    }

    /**
     * 获取用户增长趋势
     *
     * @param days 统计天数
     * @return 用户增长趋势
     */
    private List<UserStatisticsResponse.UserGrowthTrend> getUserGrowthTrend(Integer days) {
        List<UserStatisticsResponse.UserGrowthTrend> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        // 获取日期范围
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);
        
        // 转换为Date类型
        Date start = Date.from(startDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        Date end = Date.from(today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
        
        try {
            // 查询日期范围内每天的注册用户数
            List<Map<String, Object>> dailyRegistrations = userMapper.countUsersByDateRange(start, end);
            
            // 创建日期到注册数的映射
            Map<String, Long> dateToCountMap = dailyRegistrations.stream()
                    .collect(Collectors.toMap(
                            item -> String.valueOf(item.get("date")),
                            item -> Long.valueOf(String.valueOf(item.get("count")))
                    ));
            
            // 填充每一天的数据，确保连续性
            for (int i = 0; i < days; i++) {
                LocalDate date = startDate.plusDays(i);
                String dateStr = date.format(formatter);
                
                // 获取当天累计用户数
                Date currentDate = Date.from(date.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant());
                Long cumulativeCount = userMapper.countCumulativeUsersUntil(currentDate);
                
                result.add(new UserStatisticsResponse.UserGrowthTrend(dateStr, cumulativeCount));
            }
            
        } catch (Exception e) {
            log.error("获取用户增长趋势数据失败", e);
            // 发生异常时返回空列表
            return new ArrayList<>();
        }

        return result;
    }
}
