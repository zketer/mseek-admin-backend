package com.lynn.museum.system.service.impl;

import java.util.Date;

import com.lynn.museum.common.entity.PageResult;
import com.lynn.museum.common.exception.BizException;
import cn.hutool.core.bean.BeanUtil;
import com.lynn.museum.system.dto.PermissionCreateRequest;
import com.lynn.museum.system.dto.PermissionQueryRequest;
import com.lynn.museum.system.dto.PermissionResponse;
import com.lynn.museum.system.dto.PermissionUpdateRequest;
import com.lynn.museum.system.model.entity.Permission;
import com.lynn.museum.system.mapper.PermissionMapper;
import com.lynn.museum.system.mapper.RolePermissionMapper;
import com.lynn.museum.system.service.PermissionService;
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
 * 权限服务实现类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {

    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;

    @Override
    public PermissionResponse getById(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BizException("权限不存在");
        }
        return convertToResponse(permission);
    }

    @Override
    public Permission getByPermissionCode(String permissionCode) {
        return permissionMapper.selectByPermissionCode(permissionCode);
    }

    @Override
    public PageResult<PermissionResponse> getPage(PermissionQueryRequest query) {
        // 查询权限列表
        List<Permission> permissions = permissionMapper.selectAll();
        
        // 根据查询条件过滤
        List<Permission> filteredPermissions = permissions.stream()
                .filter(permission -> {
                    // 权限类型过滤
                    if (query.getPermissionType() != null && query.getPermissionType() > 0) {
                        if (!query.getPermissionType().equals(permission.getPermissionType())) {
                            return false;
                        }
                    }
                    
                    // 权限名称过滤
                    if (StringUtils.hasText(query.getPermissionName())) {
                        if (!permission.getPermissionName().contains(query.getPermissionName())) {
                            return false;
                        }
                    }
                    
                    // 权限编码过滤
                    if (StringUtils.hasText(query.getPermissionCode())) {
                        if (!permission.getPermissionCode().contains(query.getPermissionCode())) {
                            return false;
                        }
                    }
                    
                    // 状态过滤
                    if (query.getStatus() != null) {
                        if (!query.getStatus().equals(permission.getStatus())) {
                            return false;
                        }
                    }
                    
                    return true;
                })
                .collect(Collectors.toList());
        
        Long total = (long) filteredPermissions.size();
        
        // 分页处理
        int pageNum = query.getPageNum() != null ? query.getPageNum() : 1;
        int pageSize = query.getPageSize() != null ? query.getPageSize() : 10;
        int startIndex = (pageNum - 1) * pageSize;
        int endIndex = Math.min(startIndex + pageSize, filteredPermissions.size());
        
        List<Permission> pagedPermissions = filteredPermissions.subList(
                Math.max(0, startIndex), 
                Math.max(0, endIndex)
        );
        
        // 转换为响应对象
        List<PermissionResponse> responses = pagedPermissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        return PageResult.of(responses, total, pageNum, pageSize);
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        List<Permission> permissions = permissionMapper.selectAll();
        return permissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionResponse> getEnabledPermissions() {
        List<Permission> permissions = permissionMapper.selectEnabled();
        return permissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionResponse> getPermissionTree(Integer permissionType) {
        List<Permission> allPermissions = permissionMapper.selectAll();
        
        // 如果指定了权限类型，则过滤数据
        if (permissionType != null && permissionType > 0) {
            // 对于非菜单类型，需要包含所有权限来构建完整的树形结构
            // 然后在 buildPermissionTree 中进行类型过滤
            return buildPermissionTree(allPermissions, null, permissionType);
        }
        
        return buildPermissionTree(allPermissions, null, null);
    }

    @Override
    public List<PermissionResponse> getPermissionTreeByType() {
        List<PermissionResponse> result = new ArrayList<>();
        
        // 创建三个类型分组
        PermissionResponse menuGroup = new PermissionResponse();
        menuGroup.setId(-1L);
        menuGroup.setPermissionName("菜单权限");
        menuGroup.setPermissionCode("MENU_GROUP");
        menuGroup.setPermissionType(1);
        menuGroup.setParentId(0L);
        menuGroup.setStatus(1);
        menuGroup.setChildren(getPermissionTree(1));
        result.add(menuGroup);
        
        PermissionResponse operationGroup = new PermissionResponse();
        operationGroup.setId(-2L);
        operationGroup.setPermissionName("操作权限");
        operationGroup.setPermissionCode("OPERATION_GROUP");
        operationGroup.setPermissionType(2);
        operationGroup.setParentId(0L);
        operationGroup.setStatus(1);
        operationGroup.setChildren(getAllPermissionsByType(2));
        result.add(operationGroup);
        
        PermissionResponse apiGroup = new PermissionResponse();
        apiGroup.setId(-3L);
        apiGroup.setPermissionName("接口权限");
        apiGroup.setPermissionCode("API_GROUP");
        apiGroup.setPermissionType(3);
        apiGroup.setParentId(0L);
        apiGroup.setStatus(1);
        apiGroup.setChildren(getAllPermissionsByType(3));
        result.add(apiGroup);
        
        return result;
    }
    
    /**
     * 获取指定类型的所有权限（包括有父节点的权限）
     */
    private List<PermissionResponse> getAllPermissionsByType(Integer permissionType) {
        List<Permission> allPermissions = permissionMapper.selectAll();
        
        // 过滤出指定类型的权限
        List<Permission> filteredPermissions = allPermissions.stream()
                .filter(p -> p.getPermissionType().equals(permissionType))
                .collect(Collectors.toList());
        
        // 构建完整的树形结构
        return buildPermissionTree(allPermissions, null, permissionType);
    }

    @Override
    public List<PermissionResponse> getPermissionsByParentId(Long parentId, Integer permissionType) {
        List<Permission> permissions = permissionMapper.selectByParentId(parentId);
        
        // 如果指定了权限类型，则过滤数据
        if (permissionType != null && permissionType > 0) {
            permissions = permissions.stream()
                    .filter(p -> p.getPermissionType().equals(permissionType))
                    .collect(Collectors.toList());
        }
        
        return permissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createPermission(PermissionCreateRequest request) {
        // 校验权限编码唯一性
        if (existsByPermissionCode(request.getPermissionCode(), null)) {
            throw new BizException("权限编码已存在");
        }
        
        // 创建权限对象
        Permission permission = new Permission();
        BeanUtil.copyProperties(request, permission);
        permission.setCreateAt(new Date());
        permission.setUpdateAt(new Date());
        
        // 插入权限
        permissionMapper.insert(permission);
        
        log.info("创建权限成功，权限ID: {}, 权限编码: {}", permission.getId(), permission.getPermissionCode());
        return permission.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePermission(PermissionUpdateRequest request) {
        // 检查权限是否存在
        Permission existPermission = permissionMapper.selectById(request.getId());
        if (existPermission == null) {
            throw new BizException("权限不存在");
        }
        
        // 校验权限编码唯一性
        if (existsByPermissionCode(request.getPermissionCode(), request.getId())) {
            throw new BizException("权限编码已存在");
        }
        
        // 更新权限信息
        Permission permission = new Permission();
        BeanUtil.copyProperties(request, permission);
        permission.setUpdateAt(new Date());
        
        permissionMapper.updateById(permission);
        
        log.info("更新权限成功，权限ID: {}, 权限编码: {}", permission.getId(), permission.getPermissionCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePermission(Long id) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BizException("权限不存在");
        }
        
        // 检查是否有子权限
        List<Permission> children = permissionMapper.selectByParentId(id);
        if (!CollectionUtils.isEmpty(children)) {
            throw new BizException("该权限存在子权限，无法删除");
        }
        
        // 检查是否有角色使用该权限
        List<Long> roleIds = rolePermissionMapper.selectRoleIdsByPermissionId(id);
        if (!CollectionUtils.isEmpty(roleIds)) {
            throw new BizException("该权限已被角色使用，无法删除");
        }
        
        // 逻辑删除权限
        permissionMapper.deleteById(id);
        
        log.info("删除权限成功，权限ID: {}, 权限编码: {}", id, permission.getPermissionCode());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteBatchPermissions(List<Long> ids) {
        if (CollectionUtils.isEmpty(ids)) {
            return;
        }
        
        // 检查是否有子权限或角色使用
        for (Long id : ids) {
            List<Permission> children = permissionMapper.selectByParentId(id);
            if (!CollectionUtils.isEmpty(children)) {
                throw new BizException("存在权限有子权限，无法删除");
            }
            
            List<Long> roleIds = rolePermissionMapper.selectRoleIdsByPermissionId(id);
            if (!CollectionUtils.isEmpty(roleIds)) {
                throw new BizException("存在权限已被角色使用，无法删除");
            }
        }
        
        // 批量逻辑删除权限
        permissionMapper.deleteBatchByIds(ids);
        
        log.info("批量删除权限成功，权限ID列表: {}", ids);
    }

    @Override
    public void updatePermissionStatus(Long id, Integer status) {
        Permission permission = permissionMapper.selectById(id);
        if (permission == null) {
            throw new BizException("权限不存在");
        }
        
        Permission updatePermission = new Permission();
        updatePermission.setId(id);
        updatePermission.setStatus(status);
        updatePermission.setUpdateAt(new Date());
        
        permissionMapper.updateById(updatePermission);
        
        log.info("更新权限状态成功，权限ID: {}, 状态: {}", id, status);
    }

    @Override
    public boolean existsByPermissionCode(String permissionCode, Long excludeId) {
        if (!StringUtils.hasText(permissionCode)) {
            return false;
        }
        return permissionMapper.existsByPermissionCode(permissionCode, excludeId);
    }

    @Override
    public List<PermissionResponse> getPermissionsByUserId(Long userId) {
        List<Permission> permissions = permissionMapper.selectByUserId(userId);
        return permissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PermissionResponse> getPermissionsByRoleId(Long roleId) {
        List<Permission> permissions = permissionMapper.selectByRoleId(roleId);
        return permissions.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int initDemoData() {
        // 若已有数据，则不重复初始化
        List<Permission> exists = permissionMapper.selectAll();
        if (exists != null && !exists.isEmpty()) {
            return 0;
        }

        List<Permission> toInsert = new ArrayList<>();

        // 顶级-系统管理（菜单）
        Permission system = new Permission();
        system.setPermissionName("系统管理");
        system.setPermissionCode("system");
        system.setPermissionType(1);
        system.setStatus(1);
        system.setCreateAt(new Date());
        system.setUpdateAt(new Date());
        toInsert.add(system);

        // 顶级-用户管理（菜单）
        Permission user = new Permission();
        user.setPermissionName("用户管理");
        user.setPermissionCode("system:system");
        user.setPermissionType(1);
        user.setStatus(1);
        user.setCreateAt(new Date());
        user.setUpdateAt(new Date());
        toInsert.add(user);

        // API 示例
        Permission apiGetProfile = new Permission();
        apiGetProfile.setPermissionName("获取用户信息");
        apiGetProfile.setPermissionCode("GET:/api/profile/info");
        apiGetProfile.setPermissionType(3);
        apiGetProfile.setStatus(1);
        apiGetProfile.setCreateAt(new Date());
        apiGetProfile.setUpdateAt(new Date());
        toInsert.add(apiGetProfile);

        // 操作按钮示例
        Permission opQueryUser = new Permission();
        opQueryUser.setPermissionName("用户查询");
        opQueryUser.setPermissionCode("system:system:query");
        opQueryUser.setPermissionType(2);
        opQueryUser.setStatus(1);
        opQueryUser.setCreateAt(new Date());
        opQueryUser.setUpdateAt(new Date());
        toInsert.add(opQueryUser);

        // 先插入，获取ID
        for (Permission p : toInsert) {
            permissionMapper.insert(p);
        }

        // 维护简单层级：将部分设为 system 的子节点
        for (Permission p : toInsert) {
            if (!"系统管理".equals(p.getPermissionName())) {
                p.setParentId(system.getId());
                p.setUpdateAt(new Date());
                permissionMapper.updateById(p);
            }
        }

        return toInsert.size();
    }

    /**
     * 转换为响应对象
     */
    private PermissionResponse convertToResponse(Permission permission) {
        PermissionResponse response = new PermissionResponse();
        BeanUtil.copyProperties(permission, response);
        return response;
    }

    /**
     * 构建权限树
     */
    private List<PermissionResponse> buildPermissionTree(List<Permission> permissions, Long parentId, Integer permissionType) {
        List<PermissionResponse> tree = new ArrayList<>();
        
        for (Permission permission : permissions) {
            // 修复：数据库中使用0表示根节点，null表示无父节点
            Long currentParentId = permission.getParentId();
            if (currentParentId == null) {
                // 将null转换为0
                currentParentId = 0L;
            }
            
            if ((parentId == null && currentParentId == 0) || 
                (parentId != null && parentId.equals(currentParentId))) {
                
                PermissionResponse response = convertToResponse(permission);
                
                // 递归查找子权限
                List<PermissionResponse> children = buildPermissionTree(permissions, permission.getId(), permissionType);
                
                // 如果指定了权限类型，需要特殊处理
                if (permissionType != null && permissionType > 0) {
                    // 如果当前节点不是指定类型，但有指定类型的子节点，则保留该节点
                    if (!permission.getPermissionType().equals(permissionType)) {
                        if (children.isEmpty()) {
                            // 没有指定类型的子节点，跳过
                            continue;
                        }
                        // 有指定类型的子节点，保留该节点但清空其非指定类型的子节点
                        response.setChildren(children);
                    } else {
                        // 当前节点是指定类型，保留
                        response.setChildren(children);
                    }
                } else {
                    // 没有类型过滤，正常处理
                    response.setChildren(children);
                }
                
                tree.add(response);
            }
        }
        
        return tree;
    }

}