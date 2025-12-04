package com.lynn.museum.info.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.info.entity.FileBusinessRelation;
import com.lynn.museum.info.enums.BusinessTypeEnum;
import com.lynn.museum.info.enums.RelationTypeEnum;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 文件业务关联 Mapper 接口
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Mapper
public interface FileBusinessRelationMapper extends BaseMapper<FileBusinessRelation> {

    /**
     * 根据业务ID和业务类型查询文件关联
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 文件关联列表
     */
    @Select("<script>" +
            "SELECT id, file_id, business_id, business_type, relation_type, sort_order, status, remark, " +
            "create_at, update_at, create_by, update_by, deleted " +
            "FROM file_business_relation " +
            "WHERE business_id = #{businessId} AND business_type = #{businessType} AND deleted = 0 " +
            "ORDER BY sort_order ASC, id ASC" +
            "</script>")
    List<FileBusinessRelation> selectByBusiness(@Param("businessId") Long businessId, 
                                              @Param("businessType") BusinessTypeEnum businessType);

    /**
     * 根据业务ID、业务类型和关系类型查询文件关联
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param relationType 关系类型
     * @return 文件关联列表
     */
    @Select("<script>" +
            "SELECT id, file_id, business_id, business_type, relation_type, sort_order, status, remark, " +
            "create_at, update_at, create_by, update_by, deleted " +
            "FROM file_business_relation " +
            "WHERE business_id = #{businessId} AND business_type = #{businessType} " +
            "AND relation_type = #{relationType} AND deleted = 0 " +
            "ORDER BY sort_order ASC, id ASC" +
            "</script>")
    List<FileBusinessRelation> selectByBusinessAndRelation(@Param("businessId") Long businessId, 
                                                         @Param("businessType") BusinessTypeEnum businessType,
                                                         @Param("relationType") RelationTypeEnum relationType);

    /**
     * 根据文件ID查询业务关联
     * 
     * @param fileId 文件ID
     * @return 文件关联列表
     */
    @Select("<script>" +
            "SELECT id, file_id, business_id, business_type, relation_type, sort_order, status, remark, " +
            "create_at, update_at, create_by, update_by, deleted " +
            "FROM file_business_relation " +
            "WHERE file_id = #{fileId} AND deleted = 0 " +
            "ORDER BY business_type, business_id, sort_order ASC" +
            "</script>")
    List<FileBusinessRelation> selectByFileId(@Param("fileId") Long fileId);

    /**
     * 删除业务实体的所有文件关联
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE file_business_relation SET deleted = 1, update_at = NOW() " +
            "WHERE business_id = #{businessId} AND business_type = #{businessType} AND deleted = 0" +
            "</script>")
    int deleteByBusiness(@Param("businessId") Long businessId, 
                        @Param("businessType") BusinessTypeEnum businessType);

    /**
     * 删除特定业务实体和关系类型的文件关联
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param relationType 关系类型
     * @return 删除数量
     */
    @Update("<script>" +
            "UPDATE file_business_relation SET deleted = 1, update_at = NOW() " +
            "WHERE business_id = #{businessId} AND business_type = #{businessType} " +
            "AND relation_type = #{relationType} AND deleted = 0" +
            "</script>")
    int deleteByBusinessAndRelation(@Param("businessId") Long businessId, 
                                   @Param("businessType") BusinessTypeEnum businessType,
                                   @Param("relationType") RelationTypeEnum relationType);

    /**
     * 批量插入文件关联
     * 
     * @param relations 文件关联列表
     * @return 插入数量
     */
    @Insert("<script>" +
            "INSERT INTO file_business_relation (" +
            "file_id, business_id, business_type, relation_type, sort_order, " +
            "status, remark, create_at, update_at, create_by, update_by" +
            ") VALUES " +
            "<foreach collection='relations' item='item' separator=','>" +
            "(#{item.fileId}, #{item.businessId}, #{item.businessType}, #{item.relationType}, #{item.sortOrder}, " +
            "#{item.status}, #{item.remark}, NOW(), NOW(), #{item.createBy}, #{item.updateBy})" +
            "</foreach>" +
            "</script>")
    int batchInsert(@Param("relations") List<FileBusinessRelation> relations);

    /**
     * 更新排序
     * 
     * @param id 关联ID
     * @param sortOrder 新的排序值
     * @return 更新数量
     */
    @Update("<script>" +
            "UPDATE file_business_relation SET sort_order = #{sortOrder}, update_at = NOW() " +
            "WHERE id = #{id} AND deleted = 0" +
            "</script>")
    int updateSortOrder(@Param("id") Long id, @Param("sortOrder") Integer sortOrder);

    /**
     * 获取业务实体的最大排序值
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param relationType 关系类型
     * @return 最大排序值
     */
    @Select("<script>" +
            "SELECT COALESCE(MAX(sort_order), 0) " +
            "FROM file_business_relation " +
            "WHERE business_id = #{businessId} AND business_type = #{businessType} " +
            "<if test='relationType != null'>AND relation_type = #{relationType}</if>" +
            "AND deleted = 0" +
            "</script>")
    Integer getMaxSortOrder(@Param("businessId") Long businessId, 
                           @Param("businessType") BusinessTypeEnum businessType,
                           @Param("relationType") RelationTypeEnum relationType);
}
