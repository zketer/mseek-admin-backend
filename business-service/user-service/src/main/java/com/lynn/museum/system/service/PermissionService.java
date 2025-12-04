package com.lynn.museum.system.service;

import com.lynn.museum.common.entity.PageResult;
import com.lynn.museum.system.dto.PermissionCreateRequest;
import com.lynn.museum.system.dto.PermissionQueryRequest;
import com.lynn.museum.system.dto.PermissionResponse;
import com.lynn.museum.system.dto.PermissionUpdateRequest;
import com.lynn.museum.system.model.entity.Permission;

import java.util.List;

/**
 * 权限服务接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface PermissionService {

    /**
     * 根据ID查询权限
     */
    PermissionResponse getById(Long id);

    /**
     * 根据权限编码查询权限
     */
    Permission getByPermissionCode(String permissionCode);

    /**
     * 分页查询权限列表
     */
    PageResult<PermissionResponse> getPage(PermissionQueryRequest query);

    /**
     * 查询所有权限
     */
    List<PermissionResponse> getAllPermissions();

    /**
     * 查询启用的权限
     */
    List<PermissionResponse> getEnabledPermissions();

    /**
     * 查询权限树
     */
    List<PermissionResponse> getPermissionTree(Integer permissionType);

    /**
     * 查询按类型分组的权限树
     */
    List<PermissionResponse> getPermissionTreeByType();

    /**
     * 根据父级ID查询子权限
     */
    List<PermissionResponse> getPermissionsByParentId(Long parentId, Integer permissionType);

    /**
     * 创建权限
     */
    Long createPermission(PermissionCreateRequest request);

    /**
     * 更新权限
     */
    void updatePermission(PermissionUpdateRequest request);

    /**
     * 删除权限
     */
    void deletePermission(Long id);

    /**
     * 批量删除权限
     */
    void deleteBatchPermissions(List<Long> ids);

    /**
     * 启用/禁用权限
     */
    void updatePermissionStatus(Long id, Integer status);

    /**
     * 检查权限编码是否存在
     */
    boolean existsByPermissionCode(String permissionCode, Long excludeId);

    /**
     * 根据用户ID查询权限列表
     */
    List<PermissionResponse> getPermissionsByUserId(Long userId);

    /**
     * 根据角色ID查询权限列表
     */
    List<PermissionResponse> getPermissionsByRoleId(Long roleId);

    /**
     * 初始化演示权限数据（若不存在）
     * @return 新增的记录数
     */
    int initDemoData();

}