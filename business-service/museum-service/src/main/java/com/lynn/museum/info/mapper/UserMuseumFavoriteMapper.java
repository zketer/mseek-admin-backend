package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.MuseumResponse;
import com.lynn.museum.info.model.entity.UserMuseumFavorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

/**
 * 用户博物馆收藏 Mapper 接口
 *
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface UserMuseumFavoriteMapper extends BaseMapper<UserMuseumFavorite> {

    /**
     * 查询用户博物馆收藏记录（忽略逻辑删除标记）
     * 用于避免唯一约束冲突，需要查询所有记录包括已删除的
     *
     * @param userId 用户ID
     * @param museumId 博物馆ID
     * @return 收藏记录（可能是deleted=0或deleted=1）
     */
    @Select("<script>" +
            "SELECT * FROM user_museum_favorite WHERE user_id = #{userId} AND museum_id = #{museumId} LIMIT 1" +
            "</script>")
    UserMuseumFavorite selectByUserIdAndMuseumIdIgnoreLogic(
            @Param("userId") Long userId,
            @Param("museumId") Long museumId
    );

    /**
     * 强制更新 deleted 字段（绕过 @TableLogic）
     *
     * @param id 记录ID
     * @param deleted 删除状态：0=未删除，1=已删除
     * @return 影响行数
     */
    @Update("<script>" +
            "UPDATE user_museum_favorite SET deleted = #{deleted} WHERE id = #{id}" +
            "</script>")
    int updateDeletedById(@Param("id") Long id, @Param("deleted") Integer deleted);

    /**
     * 分页查询用户收藏的博物馆
     *
     * @param page 分页参数
     * @param userId 用户ID
     * @param keyword 搜索关键词
     * @param visitStatus 打卡状态：null-全部，true-已打卡，false-未打卡
     * @param sortBy 排序方式：time-收藏时间，name-名称，distance-距离
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT m.id, m.name, m.code, m.description, m.address, " +
            "m.province_code AS provinceCode, p.name AS provinceName, " +
            "m.city_code AS cityCode, c.name AS cityName, " +
            "m.district_code AS districtCode, d.name AS districtName, " +
            "m.longitude, m.latitude, m.phone, m.website, " +
            "m.open_time AS openTime, m.ticket_price AS ticketPrice, " +
            "m.ticket_description AS ticketDescription, m.capacity, m.status, m.level, m.type, " +
            "m.free_admission AS freeAdmission, m.collection_count AS collectionCount, " +
            "m.precious_items AS preciousItems, m.exhibitions, " +
            "m.education_activities AS educationActivities, m.visitor_count AS visitorCount, m.display, " +
            "uf.create_at AS favoriteTime, " +
            "CASE WHEN cr.museum_id IS NOT NULL THEN 1 ELSE 0 END AS isVisited, " +
            "cat.id AS category_id, cat.name AS category_name, cat.code AS category_code, " +
            "tag.id AS tag_id, tag.name AS tag_name, tag.code AS tag_code, tag.color AS tag_color " +
            "FROM user_museum_favorite uf " +
            "INNER JOIN museum_info m ON uf.museum_id = m.id " +
            "LEFT JOIN area_provinces p ON m.province_code = p.adcode " +
            "LEFT JOIN area_cities c ON m.city_code = c.adcode " +
            "LEFT JOIN area_districts d ON m.district_code = d.adcode " +
            "LEFT JOIN museum_category_relation mcr ON m.id = mcr.museum_id " +
            "LEFT JOIN museum_category cat ON mcr.category_id = cat.id " +
            "LEFT JOIN museum_tag_relation mtr ON m.id = mtr.museum_id " +
            "LEFT JOIN museum_tag tag ON mtr.tag_id = tag.id " +
            "LEFT JOIN checkin_record cr ON cr.user_id = uf.user_id AND cr.museum_id = m.id AND cr.deleted = 0 " +
            "WHERE uf.user_id = #{userId} AND uf.deleted = 0 AND m.deleted = 0 AND m.display = 1 " +
            "  <if test='keyword != null and keyword != \"\"'>" +
            "    AND (m.name LIKE CONCAT('%', #{keyword}, '%') " +
            "         OR m.description LIKE CONCAT('%', #{keyword}, '%') " +
            "         OR m.address LIKE CONCAT('%', #{keyword}, '%'))" +
            "  </if>" +
            "  <if test='visitStatus != null'>" +
            "    <if test='visitStatus == true'>AND cr.museum_id IS NOT NULL</if>" +
            "    <if test='visitStatus == false'>AND cr.museum_id IS NULL</if>" +
            "  </if>" +
            "<choose>" +
            "  <when test='sortBy == \"name\"'>ORDER BY m.name ASC</when>" +
            "  <when test='sortBy == \"distance\"'>ORDER BY m.city_code ASC, m.name ASC</when>" +
            "  <otherwise>ORDER BY uf.create_at DESC</otherwise>" +
            "</choose>" +
            "</script>")
    IPage<MuseumResponse> selectUserFavoriteMuseums(
            Page<MuseumResponse> page,
            @Param("userId") Long userId,
            @Param("keyword") String keyword,
            @Param("visitStatus") Boolean visitStatus,
            @Param("sortBy") String sortBy
    );

    /**
     * 检查用户是否收藏了指定博物馆
     *
     * @param userId 用户ID
     * @param museumId 博物馆ID
     * @return 是否收藏
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM user_museum_favorite " +
            "WHERE user_id = #{userId} AND museum_id = #{museumId} AND deleted = 0" +
            "</script>")
    Integer checkUserFavoriteMuseum(@Param("userId") Long userId, @Param("museumId") Long museumId);

    /**
     * 统计用户收藏的博物馆数量
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    @Select("<script>" +
            "SELECT COUNT(*) FROM user_museum_favorite uf " +
            "INNER JOIN museum_info m ON uf.museum_id = m.id " +
            "WHERE uf.user_id = #{userId} AND uf.deleted = 0 AND m.deleted = 0 AND m.display = 1" +
            "</script>")
    Integer countUserFavoriteMuseums(@Param("userId") Long userId);
}
