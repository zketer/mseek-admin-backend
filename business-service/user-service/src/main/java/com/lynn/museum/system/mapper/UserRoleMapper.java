package com.lynn.museum.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.system.model.entity.UserRole;
import org.apache.ibatis.annotations.*;

import java.util.List;
import java.util.Map;

/**
 * 用户角色关联Mapper接口 - 使用MyBatis Plus + 注解方式
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface UserRoleMapper extends BaseMapper<UserRole> {

    /**
     * 根据用户ID查询角色关联
     */
    @Select("<script>" +
            "SELECT * FROM sys_user_role WHERE user_id = #{userId}" +
            "</script>")
    List<UserRole> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询用户关联
     */
    @Select("<script>" +
            "SELECT * FROM sys_user_role WHERE role_id = #{roleId}" +
            "</script>")
    List<UserRole> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 根据用户ID查询角色ID列表
     */
    @Select("<script>" +
            "SELECT role_id FROM sys_user_role WHERE user_id = #{userId}" +
            "</script>")
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID查询用户ID列表
     */
    @Select("<script>" +
            "SELECT user_id FROM sys_user_role WHERE role_id = #{roleId}" +
            "</script>")
    List<Long> selectUserIdsByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入用户角色关联
     */
    @Insert("<script>" +
            "INSERT INTO sys_user_role (user_id, role_id) VALUES " +
            "<foreach collection='userRoles' item='item' separator=','>" +
            "(#{item.userId}, #{item.roleId})" +
            "</foreach>" +
            "</script>")
    int insertBatch(@Param("userRoles") List<UserRole> userRoles);

    /**
     * 根据用户ID删除角色关联
     */
    @Delete("<script>" +
            "DELETE FROM sys_user_role WHERE user_id = #{userId}" +
            "</script>")
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 根据角色ID删除用户关联
     */
    @Delete("<script>" +
            "DELETE FROM sys_user_role WHERE role_id = #{roleId}" +
            "</script>")
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 删除指定用户角色关联
     */
    @Delete("<script>" +
            "DELETE FROM sys_user_role WHERE user_id = #{userId} AND role_id = #{roleId}" +
            "</script>")
    int deleteByUserIdAndRoleId(@Param("userId") Long userId, @Param("roleId") Long roleId);

    /**
     * 批量删除用户角色关联
     */
    @Delete("<script>" +
            "DELETE FROM sys_user_role WHERE user_id IN " +
            "<foreach collection='userIds' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteBatchByUserIds(@Param("userIds") List<Long> userIds);
    
    /**
     * 统计角色分布
     * 返回每个角色的用户数量
     * 
     * @return 角色分布数据
     */
    @Select({"<script>",
            "SELECT r.role_name as type, COUNT(DISTINCT ur.user_id) as value ",
            "FROM sys_role r ",
            "LEFT JOIN sys_user_role ur ON r.id = ur.role_id ",
            "LEFT JOIN sys_user u ON ur.user_id = u.id AND u.deleted = 0 ",
            "WHERE r.deleted = 0 ",
            "GROUP BY r.id, r.role_name ",
            "ORDER BY value DESC",
            "</script>"})
    List<Map<String, Object>> countRoleDistribution();
}