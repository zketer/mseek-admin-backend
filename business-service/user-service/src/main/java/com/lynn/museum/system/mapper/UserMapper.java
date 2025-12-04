package com.lynn.museum.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.system.dto.UserQueryRequest;
import com.lynn.museum.system.model.entity.User;
import org.apache.ibatis.annotations.*;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 用户Mapper接口 - 使用MyBatis Plus + 注解方式
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 根据用户名查询用户
     */
    @Select("<script>" +
            "SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0" +
            "</script>")
    User selectByUsername(@Param("username") String username);

    /**
     * 根据用户名查询用户（包括软删除的）
     */
    @Select("<script>" +
            "SELECT * FROM sys_user WHERE username = #{username}" +
            "</script>")
    User selectByUsernameIncludeDeleted(@Param("username") String username);

    /**
     * 根据邮箱查询用户
     */
    @Select("<script>" +
            "SELECT * FROM sys_user WHERE email = #{email} AND deleted = 0" +
            "</script>")
    User selectByEmail(@Param("email") String email);

    /**
     * 根据邮箱查询用户（包括软删除的）
     */
    @Select("<script>" +
            "SELECT * FROM sys_user WHERE email = #{email}" +
            "</script>")
    User selectByEmailIncludeDeleted(@Param("email") String email);

    /**
     * 根据手机号查询用户
     */
    @Select("<script>" +
            "SELECT * FROM sys_user WHERE phone = #{phone} AND deleted = 0" +
            "</script>")
    User selectByPhone(@Param("phone") String phone);

    /**
     * 根据手机号查询用户（包括软删除的）
     */
    @Select("<script>" +
            "SELECT * FROM sys_user WHERE phone = #{phone}" +
            "</script>")
    User selectByPhoneIncludeDeleted(@Param("phone") String phone);

    /**
     * 分页查询用户列表（使用动态SQL）
     */
    @Select("<script>" +
            "SELECT * FROM sys_user " +
            "<where>" +
            "  <if test='query.deleted != null'>deleted = #{query.deleted}</if>" +
            "  <if test='query.deleted == null'>deleted = 0</if>" +
            "  <if test='query.username != null and query.username != \"\"'>" +
            "    AND username LIKE CONCAT('%', #{query.username}, '%')" +
            "  </if>" +
            "  <if test='query.nickname != null and query.nickname != \"\"'>" +
            "    AND nickname LIKE CONCAT('%', #{query.nickname}, '%')" +
            "  </if>" +
            "  <if test='query.email != null and query.email != \"\"'>" +
            "    AND email LIKE CONCAT('%', #{query.email}, '%')" +
            "  </if>" +
            "  <if test='query.phone != null and query.phone != \"\"'>" +
            "    AND phone LIKE CONCAT('%', #{query.phone}, '%')" +
            "  </if>" +
            "  <if test='query.status != null'>AND status = #{query.status}</if>" +
            "  <if test='query.gender != null'>AND gender = #{query.gender}</if>" +
            "</where>" +
            " ORDER BY create_at DESC" +
            " <if test='query.pageSize != null and query.pageNum != null'>" +
            "   LIMIT #{query.pageSize} OFFSET #{query.offset}" +
            " </if>" +
            "</script>")
    List<User> selectPage(@Param("query") UserQueryRequest query);

    /**
     * 查询用户总数（使用动态SQL）
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM sys_user " +
            "<where>" +
            "  <if test='query.deleted != null'>deleted = #{query.deleted}</if>" +
            "  <if test='query.deleted == null'>deleted = 0</if>" +
            "  <if test='query.username != null and query.username != \"\"'>" +
            "    AND username LIKE CONCAT('%', #{query.username}, '%')" +
            "  </if>" +
            "  <if test='query.nickname != null and query.nickname != \"\"'>" +
            "    AND nickname LIKE CONCAT('%', #{query.nickname}, '%')" +
            "  </if>" +
            "  <if test='query.email != null and query.email != \"\"'>" +
            "    AND email LIKE CONCAT('%', #{query.email}, '%')" +
            "  </if>" +
            "  <if test='query.phone != null and query.phone != \"\"'>" +
            "    AND phone LIKE CONCAT('%', #{query.phone}, '%')" +
            "  </if>" +
            "  <if test='query.status != null'>AND status = #{query.status}</if>" +
            "  <if test='query.gender != null'>AND gender = #{query.gender}</if>" +
            "</where>" +
            "</script>")
    Long selectCount(@Param("query") UserQueryRequest query);

    /**
     * 根据角色ID查询用户列表
     */
    @Select("<script>" +
            "SELECT u.* FROM sys_user u " +
            "INNER JOIN sys_user_role ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = #{roleId} AND u.deleted = 0 " +
            "ORDER BY u.create_at DESC" +
            "</script>")
    List<User> selectByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量删除用户（逻辑删除）
     */
    @Update("<script>" +
            "UPDATE sys_user SET deleted = 1, update_at = NOW() " +
            "WHERE deleted = 0 AND id IN " +
            "<foreach collection='idList' item='id' open='(' separator=',' close=')'>" +
            "#{id}" +
            "</foreach>" +
            "</script>")
    int deleteBatchIds(@Param("idList") List<Long> idList);
    
    /**
     * 按日期统计用户注册数量
     * 
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return 每日注册用户数量列表
     */
    @Select({"<script>",
            "SELECT DATE_FORMAT(create_at, '%Y-%m-%d') as date, COUNT(*) as count ",
            "FROM sys_user ",
            "WHERE create_at BETWEEN #{startDate} AND #{endDate} ",
            "GROUP BY DATE_FORMAT(create_at, '%Y-%m-%d') ",
            "ORDER BY date ASC",
            "</script>"})
    List<Map<String, Object>> countUsersByDateRange(@Param("startDate") Date startDate, @Param("endDate") Date endDate);
    
    /**
     * 按日期统计累计用户数量
     * 
     * @param date 截止日期
     * @return 截止到指定日期的累计用户数量
     */
    @Select({"<script>",
            "SELECT COUNT(*) FROM sys_user ",
            "WHERE create_at &lt;= #{date} AND deleted = 0",
            "</script>"})
    Long countCumulativeUsersUntil(@Param("date") Date date);
}