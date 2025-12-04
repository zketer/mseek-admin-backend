package com.lynn.museum.auth.service;

import com.lynn.museum.common.result.Result;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

/**
 * 认证用户服务接口
 * 专门用于Spring Security认证流程中的用户详情加载
 *
 * @author lynn
 * @since 2024-01-01
 */
public interface AuthUserService {

    /**
     * 根据用户名加载用户详情（用于Spring Security认证）
     *
     * @param username 用户名
     * @return Spring Security UserDetails对象
     * @throws UsernameNotFoundException 当用户不存在时抛出
     */
    UserDetails loadUserByUsername(String username) throws UsernameNotFoundException;

    /**
     * 获取用户权限列表
     *
     * @param userId 用户ID
     * @return 权限列表
     */
    List<String> getUserPermissions(Long userId);
}