package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.info.model.entity.MuseumCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;
import java.util.Map;

/**
 * 博物馆分类Mapper接口
 *
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface MuseumCategoryMapper extends BaseMapper<MuseumCategory> {

    /**
     * 根据博物馆ID查询分类列表
     *
     * @param museumId 博物馆ID
     * @return 分类列表
     */
    @Select("<script>" +
            "SELECT c.id, c.name, c.code, c.description, c.sort_order, c.status, " +
            "c.create_at, c.update_at, c.create_by, c.update_by, c.deleted " +
            "FROM museum_category c " +
            "INNER JOIN museum_category_relation r ON c.id = r.category_id " +
            "WHERE r.museum_id = #{museumId} AND c.deleted = 0 " +
            "ORDER BY c.sort_order ASC, c.create_at DESC" +
            "</script>")
    List<MuseumCategory> selectCategoriesByMuseumId(@Param("museumId") Long museumId);
    
    /**
     * 查询每个分类下的博物馆数量
     *
     * @return 分类统计信息，包含category_id和museum_count
     */
    @Select("<script>" +
            "SELECT mcr.category_id, COUNT(DISTINCT mcr.museum_id) as museum_count " +
            "FROM museum_category_relation mcr " +
            "JOIN museum_info mi ON mcr.museum_id = mi.id " +
            "WHERE mi.deleted = 0 " +
            "GROUP BY mcr.category_id" +
            "</script>")
    List<Map<String, Object>> selectCategoryStatistics();
}
