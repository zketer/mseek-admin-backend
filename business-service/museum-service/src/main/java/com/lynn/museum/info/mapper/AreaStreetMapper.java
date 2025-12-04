package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.info.model.entity.AreaStreet;
import org.apache.ibatis.annotations.Mapper;

/**
 * 街道数据访问层
 *
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface AreaStreetMapper extends BaseMapper<AreaStreet> {
}
