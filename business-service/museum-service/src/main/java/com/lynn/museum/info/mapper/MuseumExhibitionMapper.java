package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.ExhibitionQueryRequest;
import com.lynn.museum.info.dto.ExhibitionResponse;
import com.lynn.museum.info.model.entity.MuseumExhibition;
import org.apache.ibatis.annotations.*;

/**
 * 博物馆展览Mapper接口
 *
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface MuseumExhibitionMapper extends BaseMapper<MuseumExhibition> {

    /**
     * 分页查询展览列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT e.id, e.museum_id AS museumId, m.name AS museumName, " +
            "e.title, e.description, e.cover_image AS coverImage, " +
            "e.start_date AS startDate, e.end_date AS endDate, e.location, " +
            "e.ticket_price AS ticketPrice, e.status, e.is_permanent AS isPermanent, e.display, " +
            "e.create_at AS createTime, e.update_at AS updateTime " +
            "FROM museum_exhibition e " +
            "LEFT JOIN museum_info m ON e.museum_id = m.id " +
            "<where>" +
            "  e.deleted = 0 AND e.display = 1 " +
            "  <if test='query.museumId != null'>AND e.museum_id = #{query.museumId}</if>" +
            "  <if test='query.title != null and query.title != \"\"'>AND e.title LIKE CONCAT('%', #{query.title}, '%')</if>" +
            "  <if test='query.status != null'>AND e.status = #{query.status}</if>" +
            "  <if test='query.isPermanent != null'>AND e.is_permanent = #{query.isPermanent}</if>" +
            "</where>" +
            "ORDER BY e.create_at DESC" +
            "</script>")
    IPage<ExhibitionResponse> selectExhibitionPage(Page<MuseumExhibition> page, @Param("query") ExhibitionQueryRequest query);

    /**
     * 获取展览详情
     *
     * @param id 展览ID
     * @return 展览详情
     */
    @Select("<script>" +
            "SELECT e.id, e.museum_id AS museumId, m.name AS museumName, " +
            "e.title, e.description, e.cover_image AS coverImage, " +
            "e.start_date AS startDate, e.end_date AS endDate, e.location, " +
            "e.ticket_price AS ticketPrice, e.status, e.is_permanent AS isPermanent, e.display, " +
            "e.create_at AS createTime, e.update_at AS updateTime " +
            "FROM museum_exhibition e " +
            "LEFT JOIN museum_info m ON e.museum_id = m.id " +
            "WHERE e.id = #{id} AND e.deleted = 0 AND e.display = 1" +
            "</script>")
    ExhibitionResponse selectExhibitionById(@Param("id") Long id);

    /**
     * 分页查询最新展览列表
     * 条件：开始时间 >= 当前时间 && 当前时间 <= 结束时间
     * 排序：按开始时间从小到大排序
     *
     * @param page 分页参数
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT e.id, e.museum_id AS museumId, m.name AS museumName, " +
            "e.title, e.description, e.cover_image AS coverImage, " +
            "e.start_date AS startDate, e.end_date AS endDate, e.location, " +
            "e.ticket_price AS ticketPrice, e.status, e.is_permanent AS isPermanent, e.display, " +
            "e.create_at AS createTime, e.update_at AS updateTime " +
            "FROM museum_exhibition e " +
            "LEFT JOIN museum_info m ON e.museum_id = m.id " +
            "WHERE e.deleted = 0 AND e.status = 1 AND e.display = 1 " +
            "AND (e.is_permanent = 1 OR NOW() &lt;= e.end_date) " +
            "ORDER BY e.is_permanent DESC, e.start_date ASC" +
            "</script>")
    IPage<ExhibitionResponse> selectLatestExhibitions(Page<ExhibitionResponse> page);

    /**
     * 分页查询所有展览列表（支持过滤条件）
     *
     * @param page        分页参数
     * @param museumId    博物馆ID（可选）
     * @param title       展览标题搜索（可选）
     * @param status      状态过滤（可选）
     * @param isPermanent 是否常设展览（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT e.id, e.museum_id AS museumId, m.name AS museumName, " +
            "e.title, e.description, e.cover_image AS coverImage, " +
            "e.start_date AS startDate, e.end_date AS endDate, e.location, " +
            "e.ticket_price AS ticketPrice, e.status, e.is_permanent AS isPermanent, e.display, " +
            "e.create_at AS createTime, e.update_at AS updateTime " +
            "FROM museum_exhibition e " +
            "LEFT JOIN museum_info m ON e.museum_id = m.id " +
            "WHERE e.deleted = 0 AND e.display = 1 " +
            "  <if test='museumId != null'>AND e.museum_id = #{museumId}</if>" +
            "  <if test='title != null and title != \"\"'>AND e.title LIKE CONCAT('%', #{title}, '%')</if>" +
            "  <if test='status != null'>AND e.status = #{status}</if>" +
            "  <if test='isPermanent != null'>AND e.is_permanent = #{isPermanent}</if>" +
            "ORDER BY e.create_at DESC" +
            "</script>")
    IPage<ExhibitionResponse> selectAllExhibitions(Page<MuseumExhibition> page, 
                                                   @Param("museumId") Long museumId,
                                                   @Param("title") String title,
                                                   @Param("status") Integer status,
                                                   @Param("isPermanent") Integer isPermanent);
}