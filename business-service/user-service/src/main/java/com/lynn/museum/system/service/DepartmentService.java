package com.lynn.museum.system.service;

import com.lynn.museum.system.dto.DepartmentCreateRequest;
import com.lynn.museum.system.dto.DepartmentQueryRequest;
import com.lynn.museum.system.dto.DepartmentResponse;
import com.lynn.museum.system.dto.DepartmentUpdateRequest;

import java.util.List;

/**
 * 部门服务接口
 * 
 * @author lynn
 * @since 2024-01-01
 */
public interface DepartmentService {

    /**
     * 根据ID查询部门
     */
    DepartmentResponse getById(Long id);

    /**
     * 查询部门树
     */
    List<DepartmentResponse> getDepartmentTree(DepartmentQueryRequest query);

    /**
     * 查询部门列表
     */
    List<DepartmentResponse> getDepartmentList(DepartmentQueryRequest query);

    /**
     * 查询启用的部门列表
     */
    List<DepartmentResponse> getEnabledDepartments();

    /**
     * 根据父级ID查询子部门
     */
    List<DepartmentResponse> getChildrenByParentId(Long parentId);

    /**
     * 创建部门
     */
    Long createDepartment(DepartmentCreateRequest request);

    /**
     * 更新部门
     */
    void updateDepartment(DepartmentUpdateRequest request);

    /**
     * 删除部门
     */
    void deleteDepartment(Long id);

    /**
     * 批量删除部门
     */
    void deleteBatchDepartments(List<Long> ids);

    /**
     * 更新部门状态
     */
    void updateDepartmentStatus(Long id, Integer status);

    /**
     * 检查部门编码是否存在
     */
    boolean existsByDeptCode(String deptCode, Long excludeId);

    /**
     * 获取部门用户列表
     */
    List<Long> getDepartmentUsers(Long id);

    /**
     * 移动用户到部门
     */
    void moveUsersToDepart(Long id, List<Long> userIds);

}
