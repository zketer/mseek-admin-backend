package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.MuseumQueryRequest;
import com.lynn.museum.info.dto.MuseumResponse;
import com.lynn.museum.info.model.entity.MuseumInfo;
import org.apache.ibatis.annotations.*;

/**
 * 博物馆信息Mapper接口
 *
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface MuseumInfoMapper extends BaseMapper<MuseumInfo> {

    /**
     * 分页查询博物馆列表
     *
     * @param page  分页参数
     * @param query 查询条件
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT " +
            "m.id, m.name, m.code, m.description, m.address, " +
            "m.province_code AS provinceCode, p.name AS provinceName, " +
            "m.city_code AS cityCode, c.name AS cityName, " +
            "m.district_code AS districtCode, d.name AS districtName, " +
            "m.longitude, m.latitude, m.phone, m.website, " +
            "m.open_time AS openTime, m.ticket_price AS ticketPrice, m.ticket_description AS ticketDescription, " +
            "m.capacity, m.status, m.level, m.type, " +
            "m.free_admission AS freeAdmission, m.collection_count AS collectionCount, " +
            "m.precious_items AS preciousItems, m.exhibitions, " +
            "m.education_activities AS educationActivities, m.visitor_count AS visitorCount, m.display, " +
            "m.create_at AS createTime, m.update_at AS updateTime, " +
            "cat.id AS category_id, cat.name AS category_name, cat.code AS category_code, " +
            "COALESCE(cr.checkin_count, 0) AS checkin_count " +
            "FROM museum_info m " +
            "LEFT JOIN area_provinces p ON m.province_code = p.adcode " +
            "LEFT JOIN area_cities c ON m.city_code = c.adcode " +
            "LEFT JOIN area_districts d ON m.district_code = d.adcode " +
            "LEFT JOIN museum_category_relation mcr ON m.id = mcr.museum_id " +
            "LEFT JOIN museum_category cat ON mcr.category_id = cat.id " +
            "LEFT JOIN (" +
            "  SELECT museum_id, COUNT(*) AS checkin_count FROM checkin_record WHERE deleted = 0 GROUP BY museum_id" +
            ") cr ON m.id = cr.museum_id " +
            "<where>" +
            "  m.deleted = 0 AND m.display = 1 " +
            "  <if test='query.name != null and query.name != \"\"'>AND m.name LIKE CONCAT('%', #{query.name}, '%')</if>" +
            "  <if test='query.provinceCode != null and query.provinceCode != \"\"'>AND m.province_code = #{query.provinceCode}</if>" +
            "  <if test='query.cityCode != null and query.cityCode != \"\"'>AND m.city_code = #{query.cityCode}</if>" +
            "  <if test='query.districtCode != null and query.districtCode != \"\"'>AND m.district_code = #{query.districtCode}</if>" +
            "  <if test='query.streetCode != null and query.streetCode != \"\"'>AND m.street_code = #{query.streetCode}</if>" +
            "  <if test='query.status != null'>AND m.status = #{query.status}</if>" +
            "  <if test='query.level != null'>AND m.level = #{query.level}</if>" +
            "  <if test='query.type != null and query.type != \"\"'>AND m.type = #{query.type}</if>" +
            "  <if test='query.freeAdmission != null'>AND m.free_admission = #{query.freeAdmission}</if>" +
            "  <if test='query.minCollectionCount != null'>AND m.collection_count &gt;= #{query.minCollectionCount}</if>" +
            "  <if test='query.maxCollectionCount != null'>AND m.collection_count &lt;= #{query.maxCollectionCount}</if>" +
            "  <if test='query.categoryId != null'>AND mcr.category_id = #{query.categoryId}</if>" +
            "  <if test='query.tagId != null'>" +
            "    AND EXISTS (SELECT 1 FROM museum_tag_relation mtr WHERE mtr.museum_id = m.id AND mtr.tag_id = #{query.tagId})" +
            "  </if>" +
            "</where>" +
            "<choose>" +
            "  <when test='query.sortBy == \"hot\"'>ORDER BY COALESCE(cr.checkin_count, 0) DESC, m.create_at DESC</when>" +
            "  <when test='query.sortBy == \"collection\"'>ORDER BY m.collection_count DESC, m.create_at DESC</when>" +
            "  <otherwise>ORDER BY m.create_at DESC</otherwise>" +
            "</choose>" +
            "</script>")
    IPage<MuseumResponse> selectMuseumPage(Page<MuseumInfo> page, @Param("query") MuseumQueryRequest query);

    /**
     * 获取博物馆详情
     *
     * @param id 博物馆ID
     * @return 博物馆详情
     */
    @Select("<script>" +
            "SELECT " +
            "m.id, m.name, m.code, m.description, m.address, " +
            "m.province_code AS provinceCode, p.name AS provinceName, " +
            "m.city_code AS cityCode, c.name AS cityName, " +
            "m.district_code AS districtCode, d.name AS districtName, " +
            "m.longitude, m.latitude, m.phone, m.website, " +
            "m.open_time AS openTime, m.ticket_price AS ticketPrice, m.ticket_description AS ticketDescription, " +
            "m.capacity, m.status, m.level, m.type, " +
            "m.free_admission AS freeAdmission, m.collection_count AS collectionCount, " +
            "m.precious_items AS preciousItems, m.exhibitions, " +
            "m.education_activities AS educationActivities, m.visitor_count AS visitorCount, m.display, " +
            "m.create_at AS createTime, m.update_at AS updateTime, " +
            "cat.id AS category_id, cat.name AS category_name, cat.code AS category_code " +
            "FROM museum_info m " +
            "LEFT JOIN area_provinces p ON m.province_code = p.adcode " +
            "LEFT JOIN area_cities c ON m.city_code = c.adcode " +
            "LEFT JOIN area_districts d ON m.district_code = d.adcode " +
            "LEFT JOIN museum_category_relation mcr ON m.id = mcr.museum_id " +
            "LEFT JOIN museum_category cat ON mcr.category_id = cat.id " +
            "WHERE m.id = #{id} AND m.deleted = 0 AND m.display = 1" +
            "</script>")
    MuseumResponse selectMuseumById(@Param("id") Long id);

    /**
     * 分页查询热门博物馆列表
     * 根据用户打卡次数统计最热门的博物馆
     *
     * @param page    分页参数
     * @param name    博物馆名称（可选）
     * @return 分页结果
     */
    @Select("<script>" +
            "SELECT " +
            "m.id, m.name, m.code, m.description, m.address, " +
            "m.province_code AS provinceCode, p.name AS provinceName, " +
            "m.city_code AS cityCode, c.name AS cityName, " +
            "m.district_code AS districtCode, d.name AS districtName, " +
            "m.longitude, m.latitude, m.phone, m.website, " +
            "m.open_time AS openTime, m.ticket_price AS ticketPrice, m.ticket_description AS ticketDescription, " +
            "m.capacity, m.status, m.level, m.type, " +
            "m.free_admission AS freeAdmission, m.collection_count AS collectionCount, " +
            "m.precious_items AS preciousItems, m.exhibitions, " +
            "m.education_activities AS educationActivities, m.visitor_count AS visitorCount, m.display, " +
            "m.create_at AS createTime, m.update_at AS updateTime, " +
            "cat.id AS category_id, cat.name AS category_name, cat.code AS category_code, " +
            "tag.id AS tag_id, tag.name AS tag_name, tag.code AS tag_code, tag.color AS tag_color, " +
            "COALESCE(cr.checkin_count, 0) AS checkinCount " +
            "FROM museum_info m " +
            "LEFT JOIN area_provinces p ON m.province_code = p.adcode " +
            "LEFT JOIN area_cities c ON m.city_code = c.adcode " +
            "LEFT JOIN area_districts d ON m.district_code = d.adcode " +
            "LEFT JOIN museum_category_relation mcr ON m.id = mcr.museum_id " +
            "LEFT JOIN museum_category cat ON mcr.category_id = cat.id " +
            "LEFT JOIN museum_tag_relation mtr ON m.id = mtr.museum_id " +
            "LEFT JOIN museum_tag tag ON mtr.tag_id = tag.id " +
            "INNER JOIN (" +
            "  SELECT museum_id, COUNT(*) AS checkin_count " +
            "  FROM checkin_record WHERE deleted = 0 " +
            "  GROUP BY museum_id HAVING COUNT(*) > 0" +
            ") cr ON m.id = cr.museum_id " +
            "WHERE m.deleted = 0 AND m.status = 1 AND m.display = 1 " +
            "  <if test='name != null and name != \"\" and name.trim() != \"\"'>" +
            "    AND m.name LIKE CONCAT('%', #{name}, '%')" +
            "  </if>" +
            "ORDER BY cr.checkin_count DESC, m.visitor_count DESC, m.create_at DESC" +
            "</script>")
    IPage<MuseumResponse> selectHotMuseums(Page<MuseumResponse> page, @Param("name") String name);
}
