package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.info.model.entity.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告 Mapper 接口
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}
