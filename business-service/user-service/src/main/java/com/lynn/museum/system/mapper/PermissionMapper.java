package com.lynn.museum.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.system.model.entity.Permission;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 权限Mapper接口 - 使用MyBatis Plus + 注解方式
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface PermissionMapper extends BaseMapper<Permission> {

    /**
     * 根据权限编码查询权限
     */
    @Select("<script>" +
            "SELECT * FROM sys_permission WHERE permission_code = #{permissionCode} AND deleted = 0" +
            "</script>")
    Permission selectByPermissionCode(@Param("permissionCode") String permissionCode);

    /**
     * 根据用户ID查询权限列表
     */
    @Select("<script>" +
            "SELECT DISTINCT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0 " +
            "ORDER BY p.sort_order ASC" +
            "</script>")
    List<Permission> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询权限列表
     */
    @Select("<script>" +
            "SELECT p.* FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0 " +
            "ORDER BY p.sort_order ASC" +
            "</script>")
    List<Permission> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据权限ID列表查询权限
     */
    @Select("<script>" +
            "SELECT * FROM sys_permission " +
            "WHERE deleted = 0 AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY sort_order ASC" +
            "</script>")
    List<Permission> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 查询所有权限
     */
    @Select("<script>" +
            "SELECT * FROM sys_permission WHERE deleted = 0 ORDER BY sort_order ASC" +
            "</script>")
    List<Permission> selectAll();

    /**
     * 查询启用的权限
     */
    @Select("<script>" +
            "SELECT * FROM sys_permission WHERE deleted = 0 AND status = 1 ORDER BY sort_order ASC" +
            "</script>")
    List<Permission> selectEnabled();

    /**
     * 根据父级ID查询子权限
     */
    @Select("<script>" +
            "SELECT * FROM sys_permission WHERE parent_id = #{parentId} AND deleted = 0 ORDER BY sort_order ASC" +
            "</script>")
    List<Permission> selectByParentId(@Param("parentId") Long parentId);

    /**
     * 批量删除权限（逻辑删除）
     */
    @Update("<script>" +
            "UPDATE sys_permission SET deleted = 1, update_at = NOW() " +
            "WHERE deleted = 0 AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteBatchByIds(@Param("ids") List<Long> ids);

    /**
     * 检查权限编码是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM sys_permission " +
            "WHERE permission_code = #{permissionCode} AND deleted = 0" +
            " <if test='excludeId != null'>AND id != #{excludeId}</if>" +
            "</script>")
    boolean existsByPermissionCode(@Param("permissionCode") String permissionCode, @Param("excludeId") Long excludeId);

    /**
     * 根据用户ID查询权限编码列表
     */
    @Select("<script>" +
            "SELECT DISTINCT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "INNER JOIN sys_user_role ur ON rp.role_id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND p.deleted = 0" +
            "</script>")
    List<String> selectPermissionCodesByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询权限编码列表
     */
    @Select("<script>" +
            "SELECT p.permission_code FROM sys_permission p " +
            "INNER JOIN sys_role_permission rp ON p.id = rp.permission_id " +
            "WHERE rp.role_id = #{roleId} AND p.deleted = 0" +
            "</script>")
    List<String> selectPermissionCodesByRoleId(@Param("roleId") Long roleId);

}