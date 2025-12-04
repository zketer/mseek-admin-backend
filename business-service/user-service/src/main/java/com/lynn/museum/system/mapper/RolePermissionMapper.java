package com.lynn.museum.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.system.model.entity.RolePermission;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色权限关联Mapper接口 - 使用MyBatis Plus + 注解方式
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface RolePermissionMapper extends BaseMapper<RolePermission> {

    /**
     * 根据角色ID查询权限关联
     */
    @Select("<script>" +
            "SELECT * FROM sys_role_permission WHERE role_id = #{roleId}" +
            "</script>")
    List<RolePermission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID查询角色关联
     */
    @Select("<script>" +
            "SELECT * FROM sys_role_permission WHERE permission_id = #{permissionId}" +
            "</script>")
    List<RolePermission> selectByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 根据角色ID查询权限ID列表
     */
    @Select("<script>" +
            "SELECT permission_id FROM sys_role_permission WHERE role_id = #{roleId}" +
            "</script>")
    List<Long> selectPermissionIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID查询角色ID列表
     */
    @Select("<script>" +
            "SELECT role_id FROM sys_role_permission WHERE permission_id = #{permissionId}" +
            "</script>")
    List<Long> selectRoleIdsByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 批量插入角色权限关联
     */
    @Insert("<script>" +
            "INSERT INTO sys_role_permission (role_id, permission_id) VALUES " +
            "<foreach collection='rolePermissions' item='item' separator=','>" +
            "(#{item.roleId}, #{item.permissionId})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("rolePermissions") List<RolePermission> rolePermissions);

    /**
     * 根据角色ID删除权限关联
     */
    @Delete("<script>" +
            "DELETE FROM sys_role_permission WHERE role_id = #{roleId}" +
            "</script>")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID删除角色关联
     */
    @Delete("<script>" +
            "DELETE FROM sys_role_permission WHERE permission_id = #{permissionId}" +
            "</script>")
    int deleteByPermissionId(@Param("permissionId") Long permissionId);

    /**
     * 删除指定角色权限关联
     */
    @Delete("<script>" +
            "DELETE FROM sys_role_permission WHERE role_id = #{roleId} AND permission_id = #{permissionId}" +
            "</script>")
    int deleteByRoleIdAndPermissionId(@Param("roleId") Long roleId, @Param("permissionId") Long permissionId);

    /**
     * 批量删除角色权限关联
     */
    @Delete("<script>" +
            "DELETE FROM sys_role_permission WHERE role_id IN " +
            "<foreach collection='roleIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteBatchByRoleIds(@Param("roleIds") List<Long> roleIds);

}