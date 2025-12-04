package com.lynn.museum.file.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.file.entity.FileRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文件记录Mapper
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Mapper
public interface FileRecordMapper extends BaseMapper<FileRecord> {
    
}
