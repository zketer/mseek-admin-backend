package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.info.model.entity.AreaCity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 城市信息Mapper
 *
 * @author lynn
 * @since 2025-09-26
 */
@Mapper
public interface AreaCityMapper extends BaseMapper<AreaCity> {
    
    /**
     * 根据城市电话区号查询行政区划代码
     *
     * @param citycode 城市电话区号（如：020）
     * @return 行政区划代码（如：440100）
     */
    @Select("SELECT adcode FROM area_cities WHERE citycode = #{citycode} LIMIT 1")
    String selectAdcodeByCitycode(@Param("citycode") String citycode);
}