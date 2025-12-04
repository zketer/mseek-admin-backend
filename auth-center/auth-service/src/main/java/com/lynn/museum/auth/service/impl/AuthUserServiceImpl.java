package com.lynn.museum.auth.service.impl;

import com.lynn.museum.api.user.client.UserApiClient;
import com.lynn.museum.api.user.dto.UserBasicInfo;
import com.lynn.museum.auth.service.AuthUserService;
import com.lynn.museum.common.result.Result;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 认证用户服务实现类
 * 实现Spring Security的UserDetailsService接口
 * 专门用于JWT令牌验证时加载用户详情和权限信息
 *
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthUserServiceImpl implements AuthUserService {

    @Resource
    private UserApiClient userApiClient;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("加载用户信息: {}", username);

        try {
            // 通过Feign调用用户服务获取用户信息
            Result<UserBasicInfo> result = userApiClient.getUserByUsername(username);
            if (result == null || !result.isSuccess() || result.getData() == null) {
                throw new UsernameNotFoundException("用户不存在: " + username);
            }

            UserBasicInfo user = result.getData();

            // 检查用户状态
            if (user.getStatus() == null || user.getStatus() != 1) {
                throw new UsernameNotFoundException("用户已被禁用: " + username);
            }

            // 获取用户权限
            List<String> permissions = getUserPermissions(user.getId());
            Collection<GrantedAuthority> authorities = new ArrayList<>();
            if (permissions != null && !permissions.isEmpty()) {
                for (String permission : permissions) {
                    authorities.add(new SimpleGrantedAuthority(permission));
                }
            }

            // 构建UserDetails对象
            return org.springframework.security.core.userdetails.User.builder()
                    .username(user.getUsername())
                    .password(user.getPassword())
                    .authorities(authorities)
                    .accountExpired(false)
                    .accountLocked(user.getStatus() != 1)
                    .credentialsExpired(false)
                    .disabled(user.getStatus() != 1)
                    .build();

        } catch (Exception e) {
            log.error("加载用户信息失败: {}", username, e);
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
    }

    @Override
    public List<String> getUserPermissions(Long userId) {
        try {
            Result<List<String>> result = userApiClient.getUserPermissions(userId);
            if (result != null && result.isSuccess() && result.getData() != null) {
                return result.getData();
            }
        } catch (Exception e) {
            log.error("获取用户权限失败: {}", userId, e);
        }
        // 返回空列表而不是null
        return List.of();
    }
}
