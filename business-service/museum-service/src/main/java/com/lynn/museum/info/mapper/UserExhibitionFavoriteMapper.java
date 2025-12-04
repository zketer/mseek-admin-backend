package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.ExhibitionResponse;
import com.lynn.museum.info.model.entity.UserExhibitionFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户展览收藏 Mapper 接口
 *
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface UserExhibitionFavoriteMapper extends BaseMapper<UserExhibitionFavorite> {

    /**
     * 查询用户展览收藏记录（忽略逻辑删除标记）
     * 用于避免唯一约束冲突，需要查询所有记录包括已删除的
     *
     * @param userId 用户ID
     * @param exhibitionId 展览ID
     * @return 收藏记录（可能是deleted=0或deleted=1）
     */
    @Select("<script>" +
            "SELECT * FROM user_exhibition_favorite WHERE user_id = #{userId} AND exhibition_id = #{exhibitionId} LIMIT 1" +
            "</script>")
    UserExhibitionFavorite selectByUserIdAndExhibitionIdIgnoreLogic(
            @Param("userId") Long userId,
            @Param("exhibitionId") Long exhibitionId
    );

    /**
     * 强制更新 deleted 字段（绕过 @TableLogic）
     *
     * @param id 记录ID
     * @param deleted 删除状态：0=未删除，1=已删除
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE user_exhibition_favorite SET deleted = #{deleted} WHERE id = #{id}" +
            "</script>")
    int updateDeletedById(@Param("id") Long id, @Param("deleted") Integer deleted);

    /**
     * 分页查询用户收藏的展览
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param status 展览状态：null-全部，0-已结束，1-进行中，2-未开始
     * @param sortBy 排序方式：time-收藏时间，name-名称
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT e.id, e.museum_id AS museumId, m.name AS museumName, " +
            "e.title, e.description, e.cover_image AS coverImage, " +
            "e.start_date AS startDate, e.end_date AS endDate, e.location, " +
            "e.ticket_price AS ticketPrice, e.status, e.is_permanent AS isPermanent, e.display, " +
            "uf.create_at AS favoriteTime, e.create_at AS createTime, e.update_at AS updateTime " +
            "FROM user_exhibition_favorite uf " +
            "INNER JOIN museum_exhibition e ON uf.exhibition_id = e.id " +
            "LEFT JOIN museum_info m ON e.museum_id = m.id " +
            "WHERE uf.user_id = #{userId} AND uf.deleted = 0 AND e.deleted = 0 AND e.display = 1 " +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (e.title LIKE CONCAT('%', #{keyword}, '%') " +
            "         OR e.description LIKE CONCAT('%', #{keyword}, '%') " +
            "         OR m.name LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "  <if test='status != null'>AND e.status = #{status}</if>" +
            "<choose>" +
            "  <when test='sortBy == \"name\"'>ORDER BY e.title ASC</when>" +
            "  <otherwise>ORDER BY uf.create_at DESC</otherwise>" +
            "</choose>" +
            "</script>")
    IPage<ExhibitionResponse> selectUserFavoriteExhibitions(
            Page<ExhibitionResponse> page,
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("status") Integer status,
            @Param("sortBy") String sortBy
    );

    /**
     * 检查用户是否收藏了指定展览
     *
     * @param userId 用户ID
     * @param exhibitionId 展览ID
     * @return 是否收藏
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM user_exhibition_favorite " +
            "WHERE user_id = #{userId} AND exhibition_id = #{exhibitionId} AND deleted = 0" +
            "</script>")
    Integer checkUserFavoriteExhibition(@Param("userId") Long userId, @Param("exhibitionId") Long exhibitionId);

    /**
     * 统计用户收藏的展览数量
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM user_exhibition_favorite uf " +
            "INNER JOIN museum_exhibition e ON uf.exhibition_id = e.id " +
            "WHERE uf.user_id = #{userId} AND uf.deleted = 0 AND e.deleted = 0 AND e.display = 1" +
            "</script>")
    Integer countUserFavoriteExhibitions(@Param("userId") Long userId);
}
