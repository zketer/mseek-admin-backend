package com.lynn.museum.system.service.impl;

import java.util.Date;

import com.lynn.museum.common.entity.PageResult;
import com.lynn.museum.common.exception.BizException;
import cn.hutool.core.bean.BeanUtil;
import com.lynn.museum.system.dto.RoleCreateRequest;
import com.lynn.museum.system.dto.RoleQueryRequest;
import com.lynn.museum.system.dto.RoleResponse;
import com.lynn.museum.system.dto.RoleUpdateRequest;
import com.lynn.museum.system.model.entity.Permission;
import com.lynn.museum.system.model.entity.Role;
import com.lynn.museum.system.model.entity.RolePermission;
import com.lynn.museum.system.mapper.PermissionMapper;
import com.lynn.museum.system.mapper.RoleMapper;
import com.lynn.museum.system.mapper.RolePermissionMapper;
import com.lynn.museum.system.mapper.UserRoleMapper;
import com.lynn.museum.system.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 角色服务实现类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleMapper roleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final UserRoleMapper userRoleMapper;

    @Override
    public RoleResponse getById(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        return convertToResponse(role);
    }

    @Override
    public Role getByRoleCode(String roleCode) {
        return roleMapper.selectByRoleCode(roleCode);
    }

    @Override
    public PageResult<RoleResponse> getPage(RoleQueryRequest query) {
        // 查询角色列表
        // 这里应该有分页查询方法，暂时用selectAll
        List<Role> roles = roleMapper.selectAll();
        Long total = (long) roles.size();
        
        // 转换为响应对象
        List<RoleResponse> responses = roles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(responses, total, query.getPageNum(), query.getPageSize());
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        List<Role> roles = roleMapper.selectAll();
        return roles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<RoleResponse> getEnabledRoles() {
        List<Role> roles = roleMapper.selectEnabled();
        return roles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createRole(RoleCreateRequest request) {
        log.info("开始创建角色，请求数据: 角色名称={}, 角色编码={}, 权限数量={}", 
                request.getRoleName(), request.getRoleCode(), 
                request.getPermissionIds() != null ? request.getPermissionIds().size() : 0);
        
        // 校验角色编码唯一性
        if (existsByRoleCode(request.getRoleCode(), null)) {
            throw new BizException("角色编码已存在");
        }
        
        // 创建角色对象
        Role role = new Role();
        BeanUtil.copyProperties(request, role);
        role.setCreateAt(new Date());
        role.setUpdateAt(new Date());
        
        // 插入角色
        roleMapper.insert(role);
        log.info("角色基本信息创建完成，角色ID: {}", role.getId());
        
        // 分配权限
        if (request.getPermissionIds() != null) {
            log.info("开始分配角色权限，角色ID: {}, 权限ID列表: {}", role.getId(), request.getPermissionIds());
            assignPermissions(role.getId(), request.getPermissionIds());
            log.info("角色权限分配完成，角色ID: {}", role.getId());
        } else {
            log.info("权限ID列表为null，跳过权限分配，角色ID: {}", role.getId());
        }
        
        log.info("创建角色成功，角色ID: {}, 角色编码: {}", role.getId(), role.getRoleCode());
        return role.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateRequest request) {
        log.info("开始更新角色，请求数据: ID={}, 角色名称={}, 角色编码={}, 权限数量={}", 
                request.getId(), request.getRoleName(), request.getRoleCode(), 
                request.getPermissionIds() != null ? request.getPermissionIds().size() : 0);
        
        // 检查角色是否存在
        Role existRole = roleMapper.selectById(request.getId());
        if (existRole == null) {
            throw new BizException("角色不存在");
        }
        
        // 校验角色编码唯一性
        if (existsByRoleCode(request.getRoleCode(), request.getId())) {
            throw new BizException("角色编码已存在");
        }
        
        // 更新角色信息
        Role role = new Role();
        BeanUtil.copyProperties(request, role);
        role.setUpdateAt(new Date());
        
        roleMapper.updateById(role);
        log.info("角色基本信息更新完成，角色ID: {}", role.getId());
        
        // 更新权限分配
        if (request.getPermissionIds() != null) {
            log.info("开始更新角色权限，角色ID: {}, 权限ID列表: {}", request.getId(), request.getPermissionIds());
            // 直接调用assignPermissions，它内部会处理删除和插入
            assignPermissions(request.getId(), request.getPermissionIds());
            log.info("角色权限更新完成，角色ID: {}", request.getId());
        } else {
            log.info("权限ID列表为null，跳过权限更新，角色ID: {}", request.getId());
        }
        
        log.info("更新角色成功，角色ID: {}, 角色编码: {}", role.getId(), role.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(Long id) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        
        // 检查是否有用户使用该角色
        List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(id);
        if (!CollectionUtils.isEmpty(userIds)) {
            throw new BizException("该角色已被用户使用，无法删除");
        }
        
        // 逻辑删除角色
        roleMapper.deleteById(id);
        
        // 删除角色权限关联
        rolePermissionMapper.deleteByRoleId(id);
        
        log.info("删除角色成功，角色ID: {}, 角色编码: {}", id, role.getRoleCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchRoles(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        // 检查是否有用户使用这些角色
        for (Long id : ids) {
            List<Long> userIds = userRoleMapper.selectUserIdsByRoleId(id);
            if (!CollectionUtils.isEmpty(userIds)) {
                throw new BizException("存在角色已被用户使用，无法删除");
            }
        }
        
        // 批量逻辑删除角色
        roleMapper.deleteBatchByIds(ids);
        
        // 批量删除角色权限关联
        rolePermissionMapper.deleteBatchByRoleIds(ids);
        
        log.info("批量删除角色成功，角色ID列表: {}", ids);
    }

    @Override
    public void updateRoleStatus(Long id, Integer status) {
        Role role = roleMapper.selectById(id);
        if (role == null) {
            throw new BizException("角色不存在");
        }
        
        Role updateRole = new Role();
        updateRole.setId(id);
        updateRole.setStatus(status);
        updateRole.setUpdateAt(new Date());
        
        roleMapper.updateById(updateRole);
        
        log.info("更新角色状态成功，角色ID: {}, 状态: {}", id, status);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        log.info("开始分配角色权限，角色ID: {}, 权限ID列表: {}", roleId, permissionIds);
        
        try {
            // 先删除该角色的所有现有权限关联（防止重复键冲突）
            int deletedCount = rolePermissionMapper.deleteByRoleId(roleId);
            log.info("清理角色现有权限关联，角色ID: {}, 删除条数: {}", roleId, deletedCount);
            
            // 如果权限ID列表为空，只删除不插入
            if (CollectionUtils.isEmpty(permissionIds)) {
                log.info("权限ID列表为空，仅删除现有权限，不插入新权限，角色ID: {}", roleId);
                return;
            }
            
            // 创建角色权限关联
            List<RolePermission> rolePermissions = permissionIds.stream()
                    .map(permissionId -> {
                        RolePermission rolePermission = new RolePermission();
                        rolePermission.setRoleId(roleId);
                        rolePermission.setPermissionId(permissionId);
                        rolePermission.setCreateAt(new Date());
                        // 暂时设置默认用户ID，后续可以从Security Context获取
                        rolePermission.setCreateBy(1L);
                        return rolePermission;
                    })
                    .collect(Collectors.toList());
            
            // 批量插入权限关联
            int insertCount = rolePermissionMapper.insertBatch(rolePermissions);
            log.info("批量插入角色权限关联成功，插入条数: {}", insertCount);
            
            log.info("分配角色权限成功，角色ID: {}, 删除: {}, 插入: {}, 权限ID列表: {}", 
                    roleId, deletedCount, insertCount, permissionIds);
        } catch (Exception e) {
            log.error("分配角色权限失败，角色ID: {}, 权限ID列表: {}, 错误: {}", roleId, permissionIds, e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public List<String> getRolePermissions(Long roleId) {
        return permissionMapper.selectPermissionCodesByRoleId(roleId);
    }

    @Override
    public boolean existsByRoleCode(String roleCode, Long excludeId) {
        if (!StringUtils.hasText(roleCode)) {
            return false;
        }
        return roleMapper.existsByRoleCode(roleCode, excludeId);
    }

    @Override
    public List<RoleResponse> getRolesByUserId(Long userId) {
        List<Role> roles = roleMapper.selectByUserId(userId);
        return roles.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 转换为响应对象
     */
    private RoleResponse convertToResponse(Role role) {
        RoleResponse response = new RoleResponse();
        BeanUtil.copyProperties(role, response);
        
        // 查询角色权限
        List<Permission> permissions = permissionMapper.selectByRoleId(role.getId());
        if (!CollectionUtils.isEmpty(permissions)) {
            List<RoleResponse.PermissionInfo> permissionInfos = permissions.stream()
                    .map(permission -> {
                        RoleResponse.PermissionInfo permissionInfo = new RoleResponse.PermissionInfo();
                        permissionInfo.setId(permission.getId());
                        permissionInfo.setPermissionName(permission.getPermissionName());
                        permissionInfo.setPermissionCode(permission.getPermissionCode());
                        permissionInfo.setPermissionType(permission.getPermissionType());
                        return permissionInfo;
                    })
                    .collect(Collectors.toList());
            response.setPermissions(permissionInfos);
        } else {
            response.setPermissions(new ArrayList<>());
        }
        
        return response;
    }

}