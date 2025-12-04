package com.lynn.museum.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.system.model.entity.Role;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 角色Mapper接口 - 使用MyBatis Plus + 注解方式
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface RoleMapper extends BaseMapper<Role> {

    /**
     * 根据角色编码查询角色
     */
    @Select("<script>" +
            "SELECT * FROM sys_role WHERE role_code = #{roleCode} AND deleted = 0" +
            "</script>")
    Role selectByRoleCode(@Param("roleCode") String roleCode);

    /**
     * 根据用户ID查询角色列表
     */
    @Select("<script>" +
            "SELECT r.* FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0 " +
            "ORDER BY r.sort_order ASC" +
            "</script>")
    List<Role> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID列表查询角色
     */
    @Select("<script>" +
            "SELECT * FROM sys_role " +
            "WHERE deleted = 0 AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY sort_order ASC" +
            "</script>")
    List<Role> selectByIds(@Param("ids") List<Long> ids);

    /**
     * 根据角色ID列表查询角色编码
     */
    @Select("<script>" +
            "SELECT role_code FROM sys_role " +
            "WHERE deleted = 0 AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            " ORDER BY sort_order ASC" +
            "</script>")
    List<String> selectRoleCodesByIds(@Param("ids") List<Long> ids);

    /**
     * 查询所有角色
     */
    @Select("<script>" +
            "SELECT * FROM sys_role WHERE deleted = 0 ORDER BY sort_order ASC" +
            "</script>")
    List<Role> selectAll();

    /**
     * 查询启用的角色
     */
    @Select("<script>" +
            "SELECT * FROM sys_role WHERE deleted = 0 AND status = 1 ORDER BY sort_order ASC" +
            "</script>")
    List<Role> selectEnabled();

    /**
     * 批量删除角色（逻辑删除）
     */
    @Update("<script>" +
            "UPDATE sys_role SET deleted = 1, update_at = NOW() " +
            "WHERE deleted = 0 AND id IN " +
            "<foreach collection='ids' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteBatchByIds(@Param("ids") List<Long> ids);

    /**
     * 检查角色编码是否存在
     */
    @Select("<script>" +
            "SELECT COUNT(*) > 0 FROM sys_role " +
            "WHERE role_code = #{roleCode} AND deleted = 0" +
            " <if test='excludeId != null'>AND id != #{excludeId}</if>" +
            "</script>")
    boolean existsByRoleCode(@Param("roleCode") String roleCode, @Param("excludeId") Long excludeId);

}