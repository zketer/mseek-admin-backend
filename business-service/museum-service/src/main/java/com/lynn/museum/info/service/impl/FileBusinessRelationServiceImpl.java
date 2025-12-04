package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.info.entity.FileBusinessRelation;
import com.lynn.museum.info.enums.BusinessTypeEnum;
import com.lynn.museum.info.enums.RelationTypeEnum;
import com.lynn.museum.info.mapper.FileBusinessRelationMapper;
import com.lynn.museum.info.service.FileBusinessRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文件业务关联服务实现类
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileBusinessRelationServiceImpl extends ServiceImpl<FileBusinessRelationMapper, FileBusinessRelation> 
        implements FileBusinessRelationService {

    private final FileBusinessRelationMapper fileBusinessRelationMapper;

    @Override
    public List<FileBusinessRelation> getByBusiness(Long businessId, BusinessTypeEnum businessType) {
        if (businessId == null || businessType == null) {
            return new ArrayList<>();
        }
        return fileBusinessRelationMapper.selectByBusiness(businessId, businessType);
    }

    @Override
    public List<FileBusinessRelation> getByBusinessAndRelation(Long businessId, BusinessTypeEnum businessType, RelationTypeEnum relationType) {
        if (businessId == null || businessType == null || relationType == null) {
            return new ArrayList<>();
        }
        return fileBusinessRelationMapper.selectByBusinessAndRelation(businessId, businessType, relationType);
    }

    @Override
    public Long getMainImageFileId(Long businessId, BusinessTypeEnum businessType) {
        List<FileBusinessRelation> relations = getByBusinessAndRelation(businessId, businessType, RelationTypeEnum.MAIN_IMAGE);
        if (CollectionUtils.isEmpty(relations)) {
            return null;
        }
        return relations.get(0).getFileId();
    }

    @Override
    public List<FileBusinessRelation> getByFileId(Long fileId) {
        if (fileId == null) {
            return new ArrayList<>();
        }
        return fileBusinessRelationMapper.selectByFileId(fileId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileBusinessRelation createRelation(Long fileId, Long businessId, BusinessTypeEnum businessType, 
                                             RelationTypeEnum relationType, Long createBy) {
        return createRelation(fileId, businessId, businessType, relationType, null, null, createBy);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileBusinessRelation createRelation(Long fileId, Long businessId, BusinessTypeEnum businessType, 
                                             RelationTypeEnum relationType, Integer sortOrder, String remark, Long createBy) {
        
        // 参数验证
        if (fileId == null || businessId == null || businessType == null || relationType == null) {
            throw new BizException("创建文件关联失败：必要参数不能为空");
        }

        // 检查是否已存在相同关联
        List<FileBusinessRelation> existingRelations = getByBusinessAndRelation(businessId, businessType, relationType);
        
        // 对于主图等唯一性关系，检查是否已存在
        if (RelationTypeEnum.MAIN_IMAGE.equals(relationType) || RelationTypeEnum.THUMBNAIL.equals(relationType)) {
            if (!CollectionUtils.isEmpty(existingRelations)) {
                throw new BizException("该业务实体已存在" + relationType.getDescription() + "，请先删除现有关联");
            }
        }

        // 设置排序值
        if (sortOrder == null) {
            Integer maxSort = fileBusinessRelationMapper.getMaxSortOrder(businessId, businessType, relationType);
            sortOrder = (maxSort == null ? 0 : maxSort) + 1;
        }

        // 创建关联记录
        FileBusinessRelation relation = new FileBusinessRelation();
        relation.setFileId(fileId);
        relation.setBusinessId(businessId);
        relation.setBusinessType(businessType);
        relation.setRelationType(relationType);
        relation.setSortOrder(sortOrder);
        // 默认启用
        relation.setStatus(1);
        relation.setRemark(remark);
        relation.setCreateBy(createBy);
        relation.setUpdateBy(createBy);

        boolean saved = save(relation);
        if (!saved) {
            throw new BizException("创建文件关联失败");
        }

        log.info("创建文件关联: fileId={}, businessId={}, businessType={}, relationType={}", 
                fileId, businessId, businessType, relationType);
        
        return relation;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<FileBusinessRelation> batchCreateRelation(List<Long> fileIds, Long businessId, 
                                                        BusinessTypeEnum businessType, RelationTypeEnum relationType, Long createBy) {
        
        if (CollectionUtils.isEmpty(fileIds) || businessId == null || businessType == null || relationType == null) {
            throw new BizException("批量创建文件关联失败：必要参数不能为空");
        }

        // 获取起始排序值
        Integer maxSort = fileBusinessRelationMapper.getMaxSortOrder(businessId, businessType, relationType);
        int startSort = (maxSort == null ? 0 : maxSort) + 1;

        // 构建关联记录列表
        List<FileBusinessRelation> relations = new ArrayList<>();
        for (int i = 0; i < fileIds.size(); i++) {
            FileBusinessRelation relation = new FileBusinessRelation();
            relation.setFileId(fileIds.get(i));
            relation.setBusinessId(businessId);
            relation.setBusinessType(businessType);
            relation.setRelationType(relationType);
            relation.setSortOrder(startSort + i);
            relation.setStatus(1);
            relation.setCreateBy(createBy);
            relation.setUpdateBy(createBy);
            relations.add(relation);
        }

        // 批量插入
        int insertCount = fileBusinessRelationMapper.batchInsert(relations);
        if (insertCount != fileIds.size()) {
            throw new BizException("批量创建文件关联失败：期望插入" + fileIds.size() + "条，实际插入" + insertCount + "条");
        }

        log.info("批量创建文件关联: businessId={}, count={}", businessId, fileIds.size());
        
        return relations;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByBusiness(Long businessId, BusinessTypeEnum businessType) {
        if (businessId == null || businessType == null) {
            return false;
        }
        
        int deleteCount = fileBusinessRelationMapper.deleteByBusiness(businessId, businessType);
        log.info("删除文件关联: businessId={}, count={}", businessId, deleteCount);
        return deleteCount > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByBusinessAndRelation(Long businessId, BusinessTypeEnum businessType, RelationTypeEnum relationType) {
        if (businessId == null || businessType == null || relationType == null) {
            return false;
        }
        
        int deleteCount = fileBusinessRelationMapper.deleteByBusinessAndRelation(businessId, businessType, relationType);
        log.info("删除文件关联: businessId={}, relationType={}, count={}", businessId, relationType, deleteCount);
        return deleteCount > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteByFileId(Long fileId) {
        if (fileId == null) {
            return false;
        }
        
        LambdaQueryWrapper<FileBusinessRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileBusinessRelation::getFileId, fileId);
        queryWrapper.eq(FileBusinessRelation::getDeleted, 0);
        
        FileBusinessRelation updateEntity = new FileBusinessRelation();
        updateEntity.setDeleted(1);
        
        boolean updated = update(updateEntity, queryWrapper);
        if (updated) {
            log.info("删除文件关联: fileId={}", fileId);
        }
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateSortOrder(Long id, Integer sortOrder) {
        if (id == null || sortOrder == null) {
            return false;
        }
        
        int updateCount = fileBusinessRelationMapper.updateSortOrder(id, sortOrder);
        if (updateCount > 0) {
            log.info("更新文件关联排序: id={}, sortOrder={}", id, sortOrder);
        }
        return updateCount > 0;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileBusinessRelation replaceBusinessFile(Long fileId, Long businessId, BusinessTypeEnum businessType, 
                                                  RelationTypeEnum relationType, Long updateBy) {
        
        // 查询现有关联
        List<FileBusinessRelation> existingRelations = getByBusinessAndRelation(businessId, businessType, relationType);
        
        if (existingRelations.isEmpty()) {
            // 如果没有现有关联，创建新的关联
            log.info("创建新的文件关联: fileId={}, businessId={}, businessType={}, relationType={}", 
                    fileId, businessId, businessType, relationType);
            return createRelation(fileId, businessId, businessType, relationType, updateBy);
        } else {
            // 如果有现有关联，更新第一个关联，删除其他关联
            FileBusinessRelation firstRelation = existingRelations.get(0);
            
            // 更新第一个关联
            firstRelation.setFileId(fileId);
            firstRelation.setUpdateBy(updateBy);
            updateById(firstRelation);
            
            log.info("更新现有文件关联: id={}, 旧fileId={}, 新fileId={}, businessId={}", 
                    firstRelation.getId(), firstRelation.getFileId(), fileId, businessId);
            
            // 如果有多个关联，删除其他关联
            if (existingRelations.size() > 1) {
                for (int i = 1; i < existingRelations.size(); i++) {
                    FileBusinessRelation relation = existingRelations.get(i);
                    relation.setDeleted(1);
                    relation.setUpdateBy(updateBy);
                    updateById(relation);
                    log.info("删除多余的文件关联: id={}, fileId={}, businessId={}", 
                            relation.getId(), relation.getFileId(), businessId);
                }
            }
            
            return firstRelation;
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileBusinessRelation setMainImage(Long fileId, Long businessId, BusinessTypeEnum businessType, Long updateBy) {
        return replaceBusinessFile(fileId, businessId, businessType, RelationTypeEnum.MAIN_IMAGE, updateBy);
    }

    @Override
    public List<Long> getBusinessFileIds(Long businessId, BusinessTypeEnum businessType, RelationTypeEnum relationType) {
        List<FileBusinessRelation> relations;
        
        if (relationType != null) {
            relations = getByBusinessAndRelation(businessId, businessType, relationType);
        } else {
            relations = getByBusiness(businessId, businessType);
        }
        
        return relations.stream()
                .map(FileBusinessRelation::getFileId)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isFileInUse(Long fileId, Long businessId, BusinessTypeEnum businessType) {
        if (fileId == null) {
            return false;
        }
        
        LambdaQueryWrapper<FileBusinessRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileBusinessRelation::getFileId, fileId);
        
        if (businessId != null) {
            queryWrapper.eq(FileBusinessRelation::getBusinessId, businessId);
        }
        
        if (businessType != null) {
            queryWrapper.eq(FileBusinessRelation::getBusinessType, businessType);
        }
        
        queryWrapper.eq(FileBusinessRelation::getDeleted, 0);
        
        return count(queryWrapper) > 0;
    }

    @Override
    public Map<Long, List<Long>> getBatchBusinessFileIds(List<Long> businessIds, BusinessTypeEnum businessType, RelationTypeEnum relationType) {
        if (CollectionUtils.isEmpty(businessIds) || businessType == null) {
            return new HashMap<>();
        }
        
        LambdaQueryWrapper<FileBusinessRelation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.in(FileBusinessRelation::getBusinessId, businessIds)
                   .eq(FileBusinessRelation::getBusinessType, businessType)
                   .eq(FileBusinessRelation::getDeleted, 0);
        
        if (relationType != null) {
            queryWrapper.eq(FileBusinessRelation::getRelationType, relationType);
        }
        
        queryWrapper.orderByAsc(FileBusinessRelation::getBusinessId)
                   .orderByAsc(FileBusinessRelation::getSortOrder);
        
        List<FileBusinessRelation> relations = list(queryWrapper);
        
        // 按业务ID分组
        return relations.stream()
                .collect(Collectors.groupingBy(
                    FileBusinessRelation::getBusinessId,
                    Collectors.mapping(FileBusinessRelation::getFileId, Collectors.toList())
                ));
    }
}
