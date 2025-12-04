package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.info.model.entity.MuseumTag;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 博物馆标签Mapper接口
 *
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface MuseumTagMapper extends BaseMapper<MuseumTag> {

    /**
     * 根据博物馆ID查询标签列表
     *
     * @param museumId 博物馆ID
     * @return 标签列表
     */
    @Select("<script>" +
            "SELECT t.id, t.name, t.code, t.description, t.color, " +
            "t.create_at, t.update_at, t.create_by, t.update_by, t.deleted " +
            "FROM museum_tag t " +
            "INNER JOIN museum_tag_relation r ON t.id = r.tag_id " +
            "WHERE r.museum_id = #{museumId} AND t.deleted = 0 " +
            "ORDER BY t.create_at DESC" +
            "</script>")
    List<MuseumTag> selectTagsByMuseumId(@Param("museumId") Long museumId);
}
