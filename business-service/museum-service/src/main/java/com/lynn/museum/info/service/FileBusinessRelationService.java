package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lynn.museum.info.entity.FileBusinessRelation;
import com.lynn.museum.info.enums.BusinessTypeEnum;
import com.lynn.museum.info.enums.RelationTypeEnum;

import java.util.List;
import java.util.Map;

/**
 * 文件业务关联服务接口
 * 
 * @author Lynn
 * @since 2024-01-01
 */
public interface FileBusinessRelationService extends IService<FileBusinessRelation> {

    /**
     * 根据业务ID和业务类型查询文件关联
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 文件关联列表
     */
    List<FileBusinessRelation> getByBusiness(Long businessId, BusinessTypeEnum businessType);

    /**
     * 根据业务ID、业务类型和关系类型查询文件关联
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param relationType 关系类型
     * @return 文件关联列表
     */
    List<FileBusinessRelation> getByBusinessAndRelation(Long businessId, BusinessTypeEnum businessType, RelationTypeEnum relationType);

    /**
     * 获取业务实体的主图文件ID
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 主图文件ID，未找到返回null
     */
    Long getMainImageFileId(Long businessId, BusinessTypeEnum businessType);

    /**
     * 根据文件ID查询业务关联
     * 
     * @param fileId 文件ID
     * @return 文件关联列表
     */
    List<FileBusinessRelation> getByFileId(Long fileId);

    /**
     * 创建文件业务关联
     * 
     * @param fileId 文件ID
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param relationType 关系类型
     * @param createBy 创建者ID
     * @return 创建的关联记录
     */
    FileBusinessRelation createRelation(Long fileId, Long businessId, BusinessTypeEnum businessType, 
                                      RelationTypeEnum relationType, Long createBy);

    /**
     * 创建文件业务关联（带排序和备注）
     * 
     * @param fileId 文件ID
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param relationType 关系类型
     * @param sortOrder 排序值（可为null，自动设置）
     * @param remark 备注
     * @param createBy 创建者ID
     * @return 创建的关联记录
     */
    FileBusinessRelation createRelation(Long fileId, Long businessId, BusinessTypeEnum businessType, 
                                      RelationTypeEnum relationType, Integer sortOrder, String remark, Long createBy);

    /**
     * 批量创建文件业务关联
     * 
     * @param fileIds 文件ID列表
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param relationType 关系类型
     * @param createBy 创建者ID
     * @return 创建的关联记录列表
     */
    List<FileBusinessRelation> batchCreateRelation(List<Long> fileIds, Long businessId, 
                                                 BusinessTypeEnum businessType, RelationTypeEnum relationType, Long createBy);

    /**
     * 删除业务实体的所有文件关联
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @return 是否成功
     */
    boolean deleteByBusiness(Long businessId, BusinessTypeEnum businessType);

    /**
     * 删除特定业务实体和关系类型的文件关联
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param relationType 关系类型
     * @return 是否成功
     */
    boolean deleteByBusinessAndRelation(Long businessId, BusinessTypeEnum businessType, RelationTypeEnum relationType);

    /**
     * 删除文件的所有业务关联
     * 
     * @param fileId 文件ID
     * @return 是否成功
     */
    boolean deleteByFileId(Long fileId);

    /**
     * 更新文件关联的排序
     * 
     * @param id 关联ID
     * @param sortOrder 新的排序值
     * @return 是否成功
     */
    boolean updateSortOrder(Long id, Integer sortOrder);

    /**
     * 替换业务实体的特定关系类型文件
     * （先删除原有关联，再创建新关联）
     * 
     * @param fileId 新文件ID
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param relationType 关系类型
     * @param updateBy 更新者ID
     * @return 创建的关联记录
     */
    FileBusinessRelation replaceBusinessFile(Long fileId, Long businessId, BusinessTypeEnum businessType, 
                                           RelationTypeEnum relationType, Long updateBy);

    /**
     * 设置业务实体的主图
     * 
     * @param fileId 文件ID
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param updateBy 更新者ID
     * @return 创建的关联记录
     */
    FileBusinessRelation setMainImage(Long fileId, Long businessId, BusinessTypeEnum businessType, Long updateBy);

    /**
     * 获取业务实体的文件ID列表
     * 
     * @param businessId 业务ID
     * @param businessType 业务类型
     * @param relationType 关系类型（可为null，查询所有）
     * @return 文件ID列表
     */
    List<Long> getBusinessFileIds(Long businessId, BusinessTypeEnum businessType, RelationTypeEnum relationType);

    /**
     * 批量获取多个业务实体的文件ID列表
     * 
     * @param businessIds 业务ID列表
     * @param businessType 业务类型
     * @param relationType 关系类型（可为null，查询所有）
     * @return 业务ID到文件ID列表的映射
     */
    Map<Long, List<Long>> getBatchBusinessFileIds(List<Long> businessIds, BusinessTypeEnum businessType, RelationTypeEnum relationType);

    /**
     * 检查文件是否被业务实体使用
     * 
     * @param fileId 文件ID
     * @param businessId 业务ID（可为null，检查是否被任何业务使用）
     * @param businessType 业务类型（可为null）
     * @return 是否被使用
     */
    boolean isFileInUse(Long fileId, Long businessId, BusinessTypeEnum businessType);
}
