package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.info.model.entity.MuseumCategoryRelation;
import org.apache.ibatis.annotations.*;

/**
 * 博物馆与分类关联Mapper接口
 *
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface MuseumCategoryRelationMapper extends BaseMapper<MuseumCategoryRelation> {

    /**
     * 删除博物馆与分类关联
     *
     * @param museumId 博物馆ID
     * @return 影响行数
     */
    @Delete("<script>" +
            "DELETE FROM museum_category_relation WHERE museum_id = #{museumId}" +
            "</script>")
    int deleteByMuseumId(@Param("museumId") Long museumId);
}
