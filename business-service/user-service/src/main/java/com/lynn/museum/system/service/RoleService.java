package com.lynn.museum.system.service;

import com.lynn.museum.common.entity.PageResult;
import com.lynn.museum.system.dto.RoleCreateRequest;
import com.lynn.museum.system.dto.RoleQueryRequest;
import com.lynn.museum.system.dto.RoleResponse;
import com.lynn.museum.system.dto.RoleUpdateRequest;
import com.lynn.museum.system.model.entity.Role;

import java.util.List;

/**
 * 角色服务接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface RoleService {

    /**
     * 根据ID查询角色
     */
    RoleResponse getById(Long id);

    /**
     * 根据角色编码查询角色
     */
    Role getByRoleCode(String roleCode);

    /**
     * 分页查询角色列表
     */
    PageResult<RoleResponse> getPage(RoleQueryRequest query);

    /**
     * 查询所有角色
     */
    List<RoleResponse> getAllRoles();

    /**
     * 查询启用的角色
     */
    List<RoleResponse> getEnabledRoles();

    /**
     * 创建角色
     */
    Long createRole(RoleCreateRequest request);

    /**
     * 更新角色
     */
    void updateRole(RoleUpdateRequest request);

    /**
     * 删除角色
     */
    void deleteRole(Long id);

    /**
     * 批量删除角色
     */
    void deleteBatchRoles(List<Long> ids);

    /**
     * 启用/禁用角色
     */
    void updateRoleStatus(Long id, Integer status);

    /**
     * 分配角色权限
     */
    void assignPermissions(Long roleId, List<Long> permissionIds);

    /**
     * 获取角色权限列表
     */
    List<String> getRolePermissions(Long roleId);

    /**
     * 检查角色编码是否存在
     */
    boolean existsByRoleCode(String roleCode, Long excludeId);

    /**
     * 根据用户ID查询角色列表
     */
    List<RoleResponse> getRolesByUserId(Long userId);

}