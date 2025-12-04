package com.lynn.museum.info.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lynn.museum.api.file.client.FileApiClient;
import com.lynn.museum.info.dto.*;
import com.lynn.museum.info.entity.FileBusinessRelation;
import com.lynn.museum.info.enums.BusinessTypeEnum;
import com.lynn.museum.info.enums.RelationTypeEnum;
import com.lynn.museum.info.mapper.AppVersionMapper;
import com.lynn.museum.info.model.entity.AppVersion;
import com.lynn.museum.info.service.AppVersionService;
import com.lynn.museum.info.service.FileBusinessRelationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 应用版本服务实现
 *
 * @author lynn
 * @since 2025-10-27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AppVersionServiceImpl extends ServiceImpl<AppVersionMapper, AppVersion> implements AppVersionService {

    private final FileBusinessRelationService fileBusinessRelationService;
    private final FileApiClient fileApiClient;

    @Override
    public IPage<AppVersionResponse> getAppVersions(AppVersionQueryRequest query) {
        Page<AppVersion> page = new Page<>(query.getCurrent(), query.getPageSize());

        LambdaQueryWrapper<AppVersion> wrapper = new LambdaQueryWrapper<>();

        // 关键词搜索
        if (StringUtils.hasText(query.getKeyword())) {
            wrapper.and(w -> w.like(AppVersion::getVersionName, query.getKeyword())
                    .or()
                    .like(AppVersion::getChangeLog, query.getKeyword()));
        }

        // 平台筛选
        if (StringUtils.hasText(query.getPlatform()) && !"all".equals(query.getPlatform())) {
            wrapper.eq(AppVersion::getPlatform, query.getPlatform());
        }

        // 状态筛选
        if (StringUtils.hasText(query.getStatus())) {
            wrapper.eq(AppVersion::getStatus, query.getStatus());
        }

        // 按发布日期降序
        wrapper.orderByDesc(AppVersion::getReleaseDate, AppVersion::getVersionCode);

        Page<AppVersion> result = baseMapper.selectPage(page, wrapper);

        // 批量获取文件大小信息
        List<AppVersion> versions = result.getRecords();
        Map<Long, Long> fileSizeMap = batchGetFileSizes(versions);

        // 转换为响应对象
        Page<AppVersionResponse> responsePage = new Page<>();
        BeanUtils.copyProperties(result, responsePage, "records");
        responsePage.setRecords(versions.stream()
                .map(version -> convertToResponse(version, fileSizeMap))
                .collect(Collectors.toList()));

        return responsePage;
    }

    /**
     * 批量获取文件大小
     */
    private Map<Long, Long> batchGetFileSizes(List<AppVersion> versions) {
        Map<Long, Long> fileSizeMap = new HashMap<>();
        
        if (versions == null || versions.isEmpty()) {
            return fileSizeMap;
        }

        // 1. 收集所有版本的ID
        List<Long> versionIds = versions.stream()
                .map(AppVersion::getId)
                .collect(Collectors.toList());

        // 2. 批量查询文件关联关系
        List<FileBusinessRelation> relations = fileBusinessRelationService.lambdaQuery()
                .in(FileBusinessRelation::getBusinessId, versionIds)
                .eq(FileBusinessRelation::getBusinessType, BusinessTypeEnum.APP_VERSION)
                .eq(FileBusinessRelation::getRelationType, RelationTypeEnum.INSTALLATION_PACKAGE)
                .list();

        if (relations.isEmpty()) {
            return fileSizeMap;
        }

        // 3. 收集所有文件ID
        List<Long> fileIds = relations.stream()
                .map(FileBusinessRelation::getFileId)
                .distinct()
                .collect(Collectors.toList());

        // 4. 批量获取文件信息
        try {
            Map<String, Object> response = fileApiClient.getBatchFileInfo(fileIds);
            if (response != null && response.get("data") != null) {
                // 注意：JSON序列化后，Map的key（原本是Long）会变成String
                @SuppressWarnings("unchecked")
                Map<String, Object> dataMap = (Map<String, Object>) response.get("data");
                
                
                // 5. 构建 versionId -> fileSize 的映射
                for (FileBusinessRelation relation : relations) {
                    // 将 Long 类型的 fileId 转为 String 作为 key
                    String fileIdKey = String.valueOf(relation.getFileId());
                    Object fileRecordObj = dataMap.get(fileIdKey);
                    
                    if (fileRecordObj instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> fileRecord = (Map<String, Object>) fileRecordObj;
                        Object fileSizeObj = fileRecord.get("fileSize");
                        if (fileSizeObj != null) {
                            Long fileSize = 0L;
                            if (fileSizeObj instanceof Integer) {
                                fileSize = ((Integer) fileSizeObj).longValue();
                            } else if (fileSizeObj instanceof Long) {
                                fileSize = (Long) fileSizeObj;
                            }
                            fileSizeMap.put(relation.getBusinessId(), fileSize);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("批量获取文件信息失败: {}", e.getMessage(), e);
        }

        return fileSizeMap;
    }

    @Override
    public AppVersionResponse getAppVersionDetail(Long id) {
        AppVersion appVersion = baseMapper.selectById(id);
        if (appVersion == null) {
            throw new RuntimeException("版本不存在");
        }
        return convertToResponse(appVersion);
    }

    @Override
    @Transactional
    public Long createAppVersion(AppVersionCreateRequest request) {
        // 1. 验证版本号唯一性
        LambdaQueryWrapper<AppVersion> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(AppVersion::getVersionCode, request.getVersionCode())
                .eq(AppVersion::getPlatform, request.getPlatform());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new RuntimeException("该平台的版本号已存在");
        }

        // 2. 创建版本记录
        AppVersion appVersion = new AppVersion();
        BeanUtils.copyProperties(request, appVersion);
        appVersion.setChangeLog(JSONUtil.toJsonStr(request.getChangeLog()));
        if (appVersion.getIsLatest() == null) {
            appVersion.setIsLatest(false);
        }
        if (appVersion.getStatus() == null) {
            appVersion.setStatus("draft");
        }
        if (appVersion.getForceUpdate() == null) {
            appVersion.setForceUpdate(false);
        }
        appVersion.setDownloadCount(0);

        baseMapper.insert(appVersion);
        log.info("创建版本: id={}, name={}, platform={}", 
                 appVersion.getId(), appVersion.getVersionName(), appVersion.getPlatform());

        // 3. 创建文件关联记录
        if (request.getFileId() != null) {
            try {
                FileBusinessRelation relation = new FileBusinessRelation();
                relation.setFileId(request.getFileId());
                relation.setBusinessId(appVersion.getId());
                relation.setBusinessType(BusinessTypeEnum.APP_VERSION);
                relation.setRelationType(RelationTypeEnum.INSTALLATION_PACKAGE);
                relation.setSortOrder(1);
                relation.setStatus(1);
                fileBusinessRelationService.save(relation);
            } catch (Exception e) {
                log.error("创建文件关联失败，版本ID: {}, 文件ID: {}, 错误: {}", 
                        appVersion.getId(), request.getFileId(), e.getMessage(), e);
                // 回滚版本记录
                baseMapper.deleteById(appVersion.getId());
                throw new RuntimeException("创建文件关联失败: " + e.getMessage());
            }
        }

        // 4. 如果标记为最新版本，取消其他版本的最新标记
        if (appVersion.getIsLatest()) {
            markAsLatest(appVersion.getId());
        }

        return appVersion.getId();
    }

    @Override
    @Transactional
    public void updateAppVersion(Long id, AppVersionUpdateRequest request) {
        AppVersion appVersion = baseMapper.selectById(id);
        if (appVersion == null) {
            throw new RuntimeException("版本不存在");
        }

        // 1. 更新基本信息
        if (request.getVersionName() != null) {
            appVersion.setVersionName(request.getVersionName());
        }
        if (request.getVersionCode() != null) {
            // 验证版本号唯一性
            LambdaQueryWrapper<AppVersion> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AppVersion::getVersionCode, request.getVersionCode())
                    .eq(AppVersion::getPlatform, appVersion.getPlatform())
                    .ne(AppVersion::getId, id);
            if (baseMapper.selectCount(wrapper) > 0) {
                throw new RuntimeException("该平台的版本号已存在");
            }
            appVersion.setVersionCode(request.getVersionCode());
        }
        if (request.getPlatform() != null) {
            appVersion.setPlatform(request.getPlatform());
        }
        if (request.getReleaseDate() != null) {
            appVersion.setReleaseDate(request.getReleaseDate());
        }
        if (request.getUpdateType() != null) {
            appVersion.setUpdateType(request.getUpdateType());
        }
        if (request.getChangeLog() != null) {
            appVersion.setChangeLog(JSONUtil.toJsonStr(request.getChangeLog()));
        }
        if (request.getIsLatest() != null) {
            appVersion.setIsLatest(request.getIsLatest());
        }
        if (request.getStatus() != null) {
            appVersion.setStatus(request.getStatus());
        }
        if (request.getMinAndroidVersion() != null) {
            appVersion.setMinAndroidVersion(request.getMinAndroidVersion());
        }
        if (request.getMinIosVersion() != null) {
            appVersion.setMinIosVersion(request.getMinIosVersion());
        }
        if (request.getForceUpdate() != null) {
            appVersion.setForceUpdate(request.getForceUpdate());
        }
        if (request.getRemark() != null) {
            appVersion.setRemark(request.getRemark());
        }

        baseMapper.updateById(appVersion);

        // 2. 如果更新了文件ID，更新文件关联
        if (request.getFileId() != null) {
            // 如果提供了文件关系表ID，直接更新该记录
            if (request.getFileRelationId() != null) {
                // 直接根据关系表ID查询记录
                FileBusinessRelation relation = fileBusinessRelationService.getById(request.getFileRelationId());
                
                if (relation != null) {
                    // 如果文件ID变化了，更新现有记录
                    if (!relation.getFileId().equals(request.getFileId())) {
                        log.info("更新文件关联记录: relationId={}, 旧fileId={}, 新fileId={}", 
                                request.getFileRelationId(), relation.getFileId(), request.getFileId());
                        
                        relation.setFileId(request.getFileId());
                        relation.setUpdateBy(request.getUpdateBy());
                        fileBusinessRelationService.updateById(relation);
                    } else {
                        log.info("文件ID没有变化，不需要更新文件关联: relationId={}, fileId={}", 
                                request.getFileRelationId(), request.getFileId());
                    }
                } else {
                    log.warn("未找到指定的文件关联记录: relationId={}, 创建新记录", request.getFileRelationId());
                    // 如果没有找到关系记录，创建新的
                    createNewFileRelation(id, request.getFileId(), request.getUpdateBy());
                }
            } else {
                // 如果没有提供关系表ID，查询现有关联
                List<FileBusinessRelation> existingRelations = fileBusinessRelationService.getByBusinessAndRelation(
                        id, BusinessTypeEnum.APP_VERSION, RelationTypeEnum.INSTALLATION_PACKAGE);
                
                if (existingRelations.isEmpty()) {
                    // 如果没有现有关联，创建新的关联
                    log.info("创建新的文件关联: fileId={}, businessId={}", request.getFileId(), id);
                    createNewFileRelation(id, request.getFileId(), request.getUpdateBy());
                } else {
                    // 如果有现有关联，更新第一个
                    FileBusinessRelation existingRelation = existingRelations.get(0);
                    
                    if (!existingRelation.getFileId().equals(request.getFileId())) {
                        // 文件ID变化了，更新现有记录
                        log.info("更新现有文件关联: relationId={}, 旧fileId={}, 新fileId={}", 
                                existingRelation.getId(), existingRelation.getFileId(), request.getFileId());
                        
                        existingRelation.setFileId(request.getFileId());
                        existingRelation.setUpdateBy(request.getUpdateBy());
                        fileBusinessRelationService.updateById(existingRelation);
                    } else {
                        // 文件ID没有变化，不需要更新
                        log.info("文件ID没有变化，不需要更新文件关联: fileId={}, businessId={}", 
                                request.getFileId(), id);
                    }
                }
            }
        }

        // 3. 如果标记为最新版本，取消其他版本的最新标记
        if (appVersion.getIsLatest()) {
            markAsLatest(id);
        }

        log.info("更新版本: id={}", id);
    }

    @Override
    @Transactional
    public void deleteAppVersion(Long id) {
        AppVersion appVersion = baseMapper.selectById(id);
        if (appVersion == null) {
            throw new RuntimeException("版本不存在");
        }

        // 1. 逻辑删除版本记录
        baseMapper.deleteById(id);

        // 2. 删除文件关联记录
        fileBusinessRelationService.lambdaUpdate()
                .eq(FileBusinessRelation::getBusinessId, id)
                .eq(FileBusinessRelation::getBusinessType, BusinessTypeEnum.APP_VERSION)
                .remove();

        log.info("删除版本: id={}", id);
    }

    @Override
    public AppVersionStatsResponse getAppVersionStats() {
        AppVersionStatsResponse stats = new AppVersionStatsResponse();

        // 统计总版本数
        stats.setTotalVersions(Math.toIntExact(baseMapper.selectCount(null)));

        // 统计最新版本
        LambdaQueryWrapper<AppVersion> latestWrapper = new LambdaQueryWrapper<>();
        latestWrapper.eq(AppVersion::getIsLatest, true)
                .orderByDesc(AppVersion::getVersionCode)
                .last("LIMIT 1");
        AppVersion latestVersion = baseMapper.selectOne(latestWrapper);
        stats.setLatestVersion(latestVersion != null ? latestVersion.getVersionName() : "-");

        // 统计总下载量
        List<AppVersion> allVersions = baseMapper.selectList(null);
        long totalDownloads = allVersions.stream()
                .mapToLong(v -> v.getDownloadCount() != null ? v.getDownloadCount() : 0)
                .sum();
        stats.setTotalDownloads(totalDownloads);

        // 统计Android版本数
        LambdaQueryWrapper<AppVersion> androidWrapper = new LambdaQueryWrapper<>();
        androidWrapper.eq(AppVersion::getPlatform, "android");
        stats.setAndroidVersions(Math.toIntExact(baseMapper.selectCount(androidWrapper)));

        // 统计iOS版本数
        LambdaQueryWrapper<AppVersion> iosWrapper = new LambdaQueryWrapper<>();
        iosWrapper.eq(AppVersion::getPlatform, "ios");
        stats.setIosVersions(Math.toIntExact(baseMapper.selectCount(iosWrapper)));

        return stats;
    }

    @Override
    @Transactional
    public void updateDownloadCount(Long id) {
        AppVersion appVersion = baseMapper.selectById(id);
        if (appVersion != null) {
            appVersion.setDownloadCount((appVersion.getDownloadCount() != null ? appVersion.getDownloadCount() : 0) + 1);
            baseMapper.updateById(appVersion);
        }
    }

    @Override
    @Transactional
    public void markAsLatest(Long id) {
        AppVersion appVersion = baseMapper.selectById(id);
        if (appVersion == null) {
            throw new RuntimeException("版本不存在");
        }

        // 1. 取消同平台其他版本的最新标记
        baseMapper.update(null, new LambdaUpdateWrapper<AppVersion>()
                .eq(AppVersion::getPlatform, appVersion.getPlatform())
                .ne(AppVersion::getId, id)
                .set(AppVersion::getIsLatest, false));

        // 2. 标记当前版本为最新
        appVersion.setIsLatest(true);
        baseMapper.updateById(appVersion);

        log.info("标记最新版本: id={}, platform={}", id, appVersion.getPlatform());
    }

    @Override
    @Transactional
    public void publishVersion(Long id) {
        AppVersion appVersion = baseMapper.selectById(id);
        if (appVersion == null) {
            throw new RuntimeException("版本不存在");
        }

        appVersion.setStatus("published");
        baseMapper.updateById(appVersion);

        log.info("发布版本: id={}", id);
    }

    @Override
    @Transactional
    public void deprecateVersion(Long id) {
        AppVersion appVersion = baseMapper.selectById(id);
        if (appVersion == null) {
            throw new RuntimeException("版本不存在");
        }

        appVersion.setStatus("deprecated");
        appVersion.setIsLatest(false);
        baseMapper.updateById(appVersion);

        log.info("废弃版本: id={}", id);
    }

    @Override
    public String getDownloadUrl(Long id) {
        // 1. 查找文件关联记录
        FileBusinessRelation relation = fileBusinessRelationService.lambdaQuery()
                .eq(FileBusinessRelation::getBusinessId, id)
                .eq(FileBusinessRelation::getBusinessType, BusinessTypeEnum.APP_VERSION)
                .eq(FileBusinessRelation::getRelationType, RelationTypeEnum.INSTALLATION_PACKAGE)
                .one();

        if (relation == null) {
            log.warn("未找到版本的文件关联记录，版本ID: {}", id);
            return null;
        }

        // 2. 通过文件服务获取强制下载URL（而不是预览URL）
        String downloadUrl = null;
        try {
            Map<String, Object> response = fileApiClient.getFileDownloadUrl(relation.getFileId());
            if (response != null && response.get("data") != null) {
                downloadUrl = (String) response.get("data");
            }
        } catch (Exception e) {
            log.error("获取文件下载URL失败，文件ID: {}, 错误: {}", relation.getFileId(), e.getMessage(), e);
            return null;
        }

        // 3. 如果成功获取下载URL，自动增加下载次数
        if (downloadUrl != null) {
            try {
                updateDownloadCount(id);
            } catch (Exception e) {
                log.error("自动更新下载次数失败，版本ID: {}, 错误: {}", id, e.getMessage(), e);
                // 不影响下载URL的返回
            }
        }

        return downloadUrl;
    }

    /**
     * 转换为响应对象（用于列表查询，使用批量获取的文件大小）
     */
    private AppVersionResponse convertToResponse(AppVersion appVersion, Map<Long, Long> fileSizeMap) {
        AppVersionResponse response = new AppVersionResponse();
        BeanUtils.copyProperties(appVersion, response);

        // 解析JSON changeLog
        if (StringUtils.hasText(appVersion.getChangeLog())) {
            try {
                response.setChangeLog(JSONUtil.toList(appVersion.getChangeLog(), String.class));
            } catch (Exception e) {
                log.warn("解析更新日志失败，版本ID: {}, 错误: {}", appVersion.getId(), e.getMessage());
            }
        }

        // 从批量获取的Map中获取文件大小
        Long fileSize = fileSizeMap.get(appVersion.getId());
        if (fileSize != null) {
            response.setFileSize(formatFileSize(fileSize));
        } else {
            response.setFileSize("未知");
        }

        return response;
    }

    /**
     * 转换为响应对象（用于详情查询，单独获取文件信息）
     */
    private AppVersionResponse convertToResponse(AppVersion appVersion) {
        AppVersionResponse response = new AppVersionResponse();
        BeanUtils.copyProperties(appVersion, response);

        // 解析JSON changeLog
        if (StringUtils.hasText(appVersion.getChangeLog())) {
            try {
                response.setChangeLog(JSONUtil.toList(appVersion.getChangeLog(), String.class));
            } catch (Exception e) {
                log.warn("解析更新日志失败，版本ID: {}, 错误: {}", appVersion.getId(), e.getMessage());
            }
        }

        // 获取文件信息
        FileBusinessRelation relation = fileBusinessRelationService.lambdaQuery()
                .eq(FileBusinessRelation::getBusinessId, appVersion.getId())
                .eq(FileBusinessRelation::getBusinessType, BusinessTypeEnum.APP_VERSION)
                .eq(FileBusinessRelation::getRelationType, RelationTypeEnum.INSTALLATION_PACKAGE)
                .one();

        if (relation != null) {
            // 填充文件关系表ID和文件ID
            response.setFileRelationId(relation.getId());
            response.setFileId(relation.getFileId());
            
            log.debug("填充文件关系表信息，关系表ID: {}, 文件ID: {}", relation.getId(), relation.getFileId());
            try {
                // 获取文件下载URL
                Map<String, Object> urlResponse = fileApiClient.getFileUrl(relation.getFileId());
                if (urlResponse != null && urlResponse.get("data") != null) {
                    response.setDownloadUrl((String) urlResponse.get("data"));
                }

                // 获取文件详情（包含文件大小）
                Map<String, Object> fileInfoResponse = fileApiClient.getFileInfo(relation.getFileId());
                if (fileInfoResponse != null && fileInfoResponse.get("data") != null) {
                    Map<String, Object> fileData = (Map<String, Object>) fileInfoResponse.get("data");
                    Object fileSizeObj = fileData.get("fileSize");
                    
                    Long fileSize = 0L;
                    if (fileSizeObj != null) {
                        if (fileSizeObj instanceof Integer) {
                            fileSize = ((Integer) fileSizeObj).longValue();
                        } else if (fileSizeObj instanceof Long) {
                            fileSize = (Long) fileSizeObj;
                        }
                    }
                    
                    response.setFileSize(formatFileSize(fileSize));
                    log.debug("获取文件大小成功，文件ID: {}, 大小: {} bytes", relation.getFileId(), fileSize);
                } else {
                    response.setFileSize("未知");
                }
            } catch (Exception e) {
                log.warn("获取文件信息失败，文件ID: {}, 错误: {}", relation.getFileId(), e.getMessage());
                response.setFileSize("未知");
            }
        }

        return response;
    }

    /**
     * 创建新的文件关联记录
     */
    private FileBusinessRelation createNewFileRelation(Long businessId, Long fileId, Long createBy) {
        FileBusinessRelation relation = new FileBusinessRelation();
        relation.setFileId(fileId);
        relation.setBusinessId(businessId);
        relation.setBusinessType(BusinessTypeEnum.APP_VERSION);
        relation.setRelationType(RelationTypeEnum.INSTALLATION_PACKAGE);
        relation.setSortOrder(1);
        relation.setStatus(1);
        relation.setCreateBy(createBy);
        relation.setUpdateBy(createBy);
        fileBusinessRelationService.save(relation);
        
        log.info("创建新的文件关联记录成功: id={}, fileId={}, businessId={}", 
                relation.getId(), fileId, businessId);
                
        return relation;
    }
    
    /**
     * 格式化文件大小
     */
    private String formatFileSize(Long bytes) {
        if (bytes == null || bytes == 0) {
            return "0 B";
        }

        DecimalFormat df = new DecimalFormat("#.#");
        if (bytes < 1024) {
            return bytes + " B";
        } else if (bytes < 1024 * 1024) {
            return df.format(bytes / 1024.0) + " KB";
        } else if (bytes < 1024 * 1024 * 1024) {
            return df.format(bytes / (1024.0 * 1024.0)) + " MB";
        } else {
            return df.format(bytes / (1024.0 * 1024.0 * 1024.0)) + " GB";
        }
    }

    @Override
    public Map<String, AppVersionResponse> getLatestVersions() {
        Map<String, AppVersionResponse> result = new HashMap<>();

        // 查询Android最新版本
        AppVersion androidLatest = lambdaQuery()
                .eq(AppVersion::getPlatform, "android")
                .eq(AppVersion::getStatus, "published")
                .orderByDesc(AppVersion::getVersionCode)
                .last("LIMIT 1")
                .one();

        if (androidLatest != null) {
            result.put("android", convertToResponse(androidLatest));
        }

        // 查询iOS最新版本
        AppVersion iosLatest = lambdaQuery()
                .eq(AppVersion::getPlatform, "ios")
                .eq(AppVersion::getStatus, "published")
                .orderByDesc(AppVersion::getVersionCode)
                .last("LIMIT 1")
                .one();

        if (iosLatest != null) {
            result.put("ios", convertToResponse(iosLatest));
        }

        return result;
    }
}

