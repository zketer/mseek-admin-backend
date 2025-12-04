package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.common.result.ResultCode;
import com.lynn.museum.info.dto.MuseumCreateRequest;
import com.lynn.museum.info.dto.MuseumQueryRequest;
import com.lynn.museum.info.dto.MuseumResponse;
import com.lynn.museum.info.dto.MuseumUpdateRequest;
import com.lynn.museum.info.mapper.*;
import com.lynn.museum.info.model.entity.*;
import com.lynn.museum.info.service.MuseumInfoService;
import com.lynn.museum.info.service.FileBusinessRelationService;
import com.lynn.museum.info.enums.BusinessTypeEnum;
import com.lynn.museum.info.enums.RelationTypeEnum;
import com.lynn.museum.api.file.client.FileApiClient;
import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 博物馆信息服务实现类
 *
 * @author lynn
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class MuseumInfoServiceImpl extends ServiceImpl<MuseumInfoMapper, MuseumInfo> implements MuseumInfoService {

    @Resource
    private MuseumInfoMapper museumInfoMapper;
    @Resource
    private MuseumCategoryMapper museumCategoryMapper;
    @Resource
    private MuseumTagMapper museumTagMapper;
    @Resource
    private MuseumCategoryRelationMapper museumCategoryRelationMapper;
    @Resource
    private MuseumTagRelationMapper museumTagRelationMapper;
    @Resource
    private FileBusinessRelationService fileBusinessRelationService;
    @Resource
    private FileApiClient fileApiClient;

    @Override
    public IPage<MuseumResponse> getMuseumPage(MuseumQueryRequest query) {
        Page<MuseumInfo> page = new Page<>(query.getPage(), query.getSize());
        IPage<MuseumResponse> pageResult = museumInfoMapper.selectMuseumPage(page, query);
        
        // 填充分类和标签信息
        if (pageResult.getRecords() != null && !pageResult.getRecords().isEmpty()) {
            for (MuseumResponse museum : pageResult.getRecords()) {
                fillCategoriesAndTags(museum);
                // 同时填充图片URL
                fillImageUrls(museum);
            }
        }
        
        return pageResult;
    }

    @Override
    @Cacheable(value = "museum_info", key = "'museum:' + #id", unless = "#result == null")
    public MuseumResponse getMuseumById(Long id) {
        MuseumResponse museum = museumInfoMapper.selectMuseumById(id);
        if (museum == null) {
            throw new BizException(ResultCode.MUSEUM_NOT_FOUND);
        }
        
        // 填充分类和标签信息
        fillCategoriesAndTags(museum);
        
        // 填充图片URL信息
        fillImageUrls(museum);
        
        return museum;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMuseum(MuseumCreateRequest request) {
        // 检查编码是否存在
        if (existsByCode(request.getCode(), null)) {
            throw new BizException(ResultCode.MUSEUM_CODE_EXISTS);
        }
        
        // 创建博物馆
        MuseumInfo museum = new MuseumInfo();
        BeanUtils.copyProperties(request, museum);
        baseMapper.insert(museum);
        
        // 保存分类关联
        saveCategoryRelations(museum.getId(), request.getCategoryIds());
        
        // 保存标签关联
        saveTagRelations(museum.getId(), request.getTagIds());
        
        // 保存文件关联
        saveFileRelations(museum.getId(), request.getFileIds());
        
        // 保存Logo关联（使用现有的通用方法）
        if (request.getLogoFileId() != null) {
            fileBusinessRelationService.replaceBusinessFile(
                request.getLogoFileId(),
                museum.getId(),
                BusinessTypeEnum.MUSEUM,
                RelationTypeEnum.LOGO,
                    // 创建者ID
                1L
            );
        }
        
        return museum.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "museum_info", key = "'museum:' + #request.id")
    public void updateMuseum(MuseumUpdateRequest request) {
        // 检查博物馆是否存在
        MuseumInfo museum = getById(request.getId());
        if (museum == null) {
            throw new BizException(ResultCode.MUSEUM_NOT_FOUND);
        }
        
        // 更新博物馆信息
        BeanUtils.copyProperties(request, museum);
        museum.setUpdateAt(new java.util.Date());
        updateById(museum);
        
        // 更新分类关联
        museumCategoryRelationMapper.deleteByMuseumId(museum.getId());
        saveCategoryRelations(museum.getId(), request.getCategoryIds());
        
        // 更新标签关联
        museumTagRelationMapper.deleteByMuseumId(museum.getId());
        saveTagRelations(museum.getId(), request.getTagIds());
        
        // 更新文件关联
        updateFileRelations(museum.getId(), request.getFileIds());
        
        // 更新Logo关联（replaceBusinessFile会先删除旧的，再保存新的）
        if (request.getLogoFileId() != null) {
            fileBusinessRelationService.replaceBusinessFile(
                request.getLogoFileId(),
                museum.getId(),
                BusinessTypeEnum.MUSEUM,
                RelationTypeEnum.LOGO,
                    // 更新者ID
                1L
            );
        } else {
            // 如果logoFileId为null，则删除旧的Logo关联
            fileBusinessRelationService.deleteByBusinessAndRelation(
                museum.getId(),
                BusinessTypeEnum.MUSEUM,
                RelationTypeEnum.LOGO
            );
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "museum_info", key = "'museum:' + #id")
    public void deleteMuseum(Long id) {
        // 检查博物馆是否存在
        MuseumInfo museum = getById(id);
        if (museum == null) {
            throw new BizException(ResultCode.MUSEUM_NOT_FOUND);
        }
        
        // 删除博物馆
        removeById(id);
        
        // 删除分类关联
        museumCategoryRelationMapper.deleteByMuseumId(id);
        
        // 删除标签关联
        museumTagRelationMapper.deleteByMuseumId(id);
    }

    @Override
    @CacheEvict(value = "museum_info", key = "'museum:' + #id")
    public void updateStatus(Long id, Integer status) {
        // 检查博物馆是否存在
        MuseumInfo museum = getById(id);
        if (museum == null) {
            throw new BizException(ResultCode.MUSEUM_NOT_FOUND);
        }
        
        // 更新状态
        museum.setStatus(status);
        updateById(museum);
    }

    @Override
    public boolean existsByCode(String code, Long excludeId) {
        LambdaQueryWrapper<MuseumInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MuseumInfo::getCode, code);
        if (excludeId != null) {
            wrapper.ne(MuseumInfo::getId, excludeId);
        }
        return count(wrapper) > 0;
    }

    /**
     * 填充博物馆的分类和标签信息
     *
     * @param museum 博物馆响应对象
     */
    private void fillCategoriesAndTags(MuseumResponse museum) {
        if (museum == null) {
            return;
        }
        
        // 查询分类信息
        List<MuseumCategory> categories = museumCategoryMapper.selectCategoriesByMuseumId(museum.getId());
        if (!CollectionUtils.isEmpty(categories)) {
            List<MuseumResponse.CategoryInfo> categoryInfos = categories.stream()
                    .map(category -> {
                        MuseumResponse.CategoryInfo info = new MuseumResponse.CategoryInfo();
                        info.setId(category.getId());
                        info.setName(category.getName());
                        info.setCode(category.getCode());
                        return info;
                    })
                    .collect(Collectors.toList());
            museum.setCategories(categoryInfos);
        } else {
            museum.setCategories(new ArrayList<>());
        }
        
        // 查询标签信息
        List<MuseumTag> tags = museumTagMapper.selectTagsByMuseumId(museum.getId());
        if (!CollectionUtils.isEmpty(tags)) {
            List<MuseumResponse.TagInfo> tagInfos = tags.stream()
                    .map(tag -> {
                        MuseumResponse.TagInfo info = new MuseumResponse.TagInfo();
                        info.setId(tag.getId());
                        info.setName(tag.getName());
                        info.setCode(tag.getCode());
                        info.setColor(tag.getColor());
                        return info;
                    })
                    .collect(Collectors.toList());
            museum.setTags(tagInfos);
        } else {
            museum.setTags(new ArrayList<>());
        }
    }

    /**
     * 获取对象中值为null的属性名数组
     * 用于BeanUtils.copyProperties忽略null值
     *
     * @param source 源对象
     * @return null属性名数组
     */
    private String[] getNullPropertyNames(Object source) {
        final java.beans.BeanInfo beanInfo;
        try {
            beanInfo = java.beans.Introspector.getBeanInfo(source.getClass());
        } catch (java.beans.IntrospectionException e) {
            return new String[0];
        }
        
        java.beans.PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
        java.util.Set<String> emptyNames = new java.util.HashSet<>();
        
        for (java.beans.PropertyDescriptor pd : propertyDescriptors) {
            try {
                Object srcValue = pd.getReadMethod().invoke(source);
                if (srcValue == null) {
                    emptyNames.add(pd.getName());
                }
            } catch (Exception e) {
                // 忽略异常
            }
        }
        
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    /**
     * 保存博物馆与分类的关联关系
     *
     * @param museumId    博物馆ID
     * @param categoryIds 分类ID列表
     */
    private void saveCategoryRelations(Long museumId, List<Long> categoryIds) {
        if (CollectionUtils.isEmpty(categoryIds)) {
            return;
        }
        
        for (Long categoryId : categoryIds) {
            MuseumCategoryRelation relation = new MuseumCategoryRelation();
            relation.setMuseumId(museumId);
            relation.setCategoryId(categoryId);
            museumCategoryRelationMapper.insert(relation);
        }
    }

    /**
     * 保存博物馆与标签的关联关系
     *
     * @param museumId 博物馆ID
     * @param tagIds   标签ID列表
     */
    private void saveTagRelations(Long museumId, List<Long> tagIds) {
        if (CollectionUtils.isEmpty(tagIds)) {
            return;
        }
        
        for (Long tagId : tagIds) {
            MuseumTagRelation relation = new MuseumTagRelation();
            relation.setMuseumId(museumId);
            relation.setTagId(tagId);
            museumTagRelationMapper.insert(relation);
        }
    }

    @Override
    public List<MuseumInfo> getRecommendedMuseums(String type, Integer limit) {
        // 移除推荐管理模块后，直接返回默认的热门博物馆
        // 根据类型返回不同的默认推荐逻辑
        switch (type) {
            case "hot_museum":
                return getDefaultHotMuseums(limit);
            case "new_exhibition":
                return getDefaultNewExhibitionMuseums(limit);
            case "nearby":
                // 附近博物馆需要位置参数，这里返回热门博物馆作为备选
                return getDefaultHotMuseums(limit);
            default:
                return getDefaultHotMuseums(limit);
        }
    }

    @Override
    public List<MuseumInfo> getNearbyMuseums(Double latitude, Double longitude, Integer radius, Integer limit) {
        // 使用简单的距离计算，可以后续优化为使用地理位置索引
        LambdaQueryWrapper<MuseumInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.isNotNull(MuseumInfo::getLatitude)
                   .isNotNull(MuseumInfo::getLongitude)
                   .eq(MuseumInfo::getStatus, 1)
                   .eq(MuseumInfo::getDisplay, 1);

        List<MuseumInfo> allMuseums = museumInfoMapper.selectList(queryWrapper);
        
        // 计算距离并筛选
        List<MuseumInfo> nearbyMuseums = allMuseums.stream()
            .filter(museum -> {
                if (museum.getLatitude() == null || museum.getLongitude() == null) {
                    return false;
                }
                double distance = calculateDistance(latitude, longitude, 
                    museum.getLatitude().doubleValue(), museum.getLongitude().doubleValue());
                return distance <= radius;
            })
            .sorted((m1, m2) -> {
                double d1 = calculateDistance(latitude, longitude,
                    m1.getLatitude().doubleValue(), m1.getLongitude().doubleValue());
                double d2 = calculateDistance(latitude, longitude,
                    m2.getLatitude().doubleValue(), m2.getLongitude().doubleValue());
                return Double.compare(d1, d2);
            })
            .limit(limit != null ? limit : 10)
            .collect(Collectors.toList());

        return nearbyMuseums;
    }

    /**
     * 获取默认热门博物馆（基于访问量）
     */
    private List<MuseumInfo> getDefaultHotMuseums(Integer limit) {
        LambdaQueryWrapper<MuseumInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MuseumInfo::getStatus, 1)
                   .eq(MuseumInfo::getDisplay, 1)
                   .orderByDesc(MuseumInfo::getVisitorCount)
                   .orderByDesc(MuseumInfo::getCreateAt);

        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }

        return museumInfoMapper.selectList(queryWrapper);
    }

    /**
     * 获取有最新展览的博物馆
     */
    private List<MuseumInfo> getDefaultNewExhibitionMuseums(Integer limit) {
        // 简化实现：返回最近创建的博物馆
        // 实际项目中可以关联展览表查询有最新展览的博物馆
        LambdaQueryWrapper<MuseumInfo> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MuseumInfo::getStatus, 1)
                   .eq(MuseumInfo::getDisplay, 1)
                   .orderByDesc(MuseumInfo::getCreateAt);

        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }

        return museumInfoMapper.selectList(queryWrapper);
    }

    /**
     * 计算两点间距离（公里）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 地球半径（公里）
        final int R = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return R * c;
    }

    /**
     * 保存文件关联关系
     */
    private void saveFileRelations(Long museumId, List<Long> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        
        try {
            // 为博物馆批量创建图片关联
            fileBusinessRelationService.batchCreateRelation(
                fileIds, 
                museumId, 
                BusinessTypeEnum.MUSEUM,
                    // 博物馆图片使用GALLERY类型
                RelationTypeEnum.GALLERY,
                    // 创建者ID，后续可以从当前用户上下文获取
                null
            );
        } catch (Exception e) {
            // 文件关联失败不应该阻止博物馆创建，只记录日志
            // TODO: 使用日志框架记录错误
            System.err.println("博物馆文件关联创建失败，博物馆ID: " + museumId + ", 错误: " + e.getMessage());
        }
    }

    /**
     * 更新文件关联关系（比对模式）
     */
    private void updateFileRelations(Long museumId, List<Long> frontendFileIds) {
        try {
            // 获取数据库中当前的文件ID列表
            List<Long> dbFileIds = fileBusinessRelationService.getBusinessFileIds(
                museumId, 
                BusinessTypeEnum.MUSEUM, 
                RelationTypeEnum.GALLERY
            );
            
            // 如果前端传入为空，处理为空列表
            if (frontendFileIds == null) {
                frontendFileIds = new ArrayList<>();
            }
            
            // 找出需要新增的文件ID（前端有，数据库没有）
            List<Long> toAdd = frontendFileIds.stream()
                .filter(id -> !dbFileIds.contains(id))
                .collect(Collectors.toList());
            
            // 找出需要删除的文件ID（数据库有，前端没有）
            List<Long> finalFrontendFileIds = frontendFileIds;
            List<Long> toDelete = dbFileIds.stream()
                .filter(id -> !finalFrontendFileIds.contains(id))
                .collect(Collectors.toList());
            
            System.out.println("博物馆ID: " + museumId);
            System.out.println("前端传入文件ID: " + frontendFileIds);
            System.out.println("数据库现有文件ID: " + dbFileIds);
            System.out.println("需要新增的文件ID: " + toAdd);
            System.out.println("需要删除的文件ID: " + toDelete);
            
            // 删除不需要的关联
            if (!CollectionUtils.isEmpty(toDelete)) {
                for (Long fileId : toDelete) {
                    // 由于没有按单个文件ID删除的方法，我们需要先删除所有，再重建
                    // 这里先记录需要删除的，最后统一处理
                }
                // 简化处理：删除所有现有关联，然后重建前端传入的所有关联
                fileBusinessRelationService.deleteByBusinessAndRelation(
                    museumId, 
                    BusinessTypeEnum.MUSEUM, 
                    RelationTypeEnum.GALLERY
                );
                
                // 重新创建前端传入的所有关联
                if (!CollectionUtils.isEmpty(frontendFileIds)) {
                    fileBusinessRelationService.batchCreateRelation(
                        frontendFileIds, 
                        museumId, 
                        BusinessTypeEnum.MUSEUM,
                        RelationTypeEnum.GALLERY,
                        null
                    );
                }
            } else if (!CollectionUtils.isEmpty(toAdd)) {
                // 如果只有新增，没有删除，直接添加新的关联
                fileBusinessRelationService.batchCreateRelation(
                    toAdd, 
                    museumId, 
                    BusinessTypeEnum.MUSEUM,
                    RelationTypeEnum.GALLERY,
                    null
                );
            }
            
        } catch (Exception e) {
            System.err.println("博物馆文件关联更新失败，博物馆ID: " + museumId + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 根据URL列表更新文件关联关系（智能更新模式）
     */
    private void updateFileRelationsByUrls(Long museumId, List<String> imageUrls) {
        try {
            // 获取当前所有的文件关联和对应的URL
            List<Long> currentFileIds = fileBusinessRelationService.getBusinessFileIds(
                museumId, 
                BusinessTypeEnum.MUSEUM, 
                RelationTypeEnum.GALLERY
            );
            
            // 获取当前文件ID对应的URL
            Map<String, Long> urlToFileIdMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(currentFileIds)) {
                try {
                    // 调用文件服务批量获取URL
                    Map<String, Object> response = fileApiClient.getBatchFileUrls(currentFileIds);
                    if (response != null && response.get("data") != null) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> fileInfos = (List<Map<String, Object>>) response.get("data");
                        
                        for (Map<String, Object> fileInfo : fileInfos) {
                            String url = (String) fileInfo.get("url");
                            Long fileId = ((Number) fileInfo.get("fileId")).longValue();
                            if (url != null && fileId != null) {
                                urlToFileIdMap.put(url, fileId);
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.println("获取文件URL映射失败: " + e.getMessage());
                }
            }
            
            // 找出应该保留的文件ID
            List<Long> keepFileIds = new ArrayList<>();
            for (String url : imageUrls) {
                Long fileId = urlToFileIdMap.get(url);
                if (fileId != null) {
                    keepFileIds.add(fileId);
                }
            }
            
            // 找出需要删除的文件ID
            List<Long> toDelete = currentFileIds.stream()
                .filter(id -> !keepFileIds.contains(id))
                .collect(Collectors.toList());
            
            // 删除不需要的关联
            if (!CollectionUtils.isEmpty(toDelete)) {
                for (Long fileId : toDelete) {
                    // 删除特定文件的关联
                    fileBusinessRelationService.deleteByBusinessAndRelation(
                        museumId, 
                        BusinessTypeEnum.MUSEUM, 
                        RelationTypeEnum.GALLERY
                    );
                    // 由于我们没有按文件ID删除的方法，先删除所有，然后重建
                    break;
                }
                
                // 重新创建需要保留的关联
                if (!CollectionUtils.isEmpty(keepFileIds)) {
                    fileBusinessRelationService.batchCreateRelation(
                        keepFileIds, 
                        museumId, 
                        BusinessTypeEnum.MUSEUM,
                        RelationTypeEnum.GALLERY,
                        null
                    );
                }
            }
            
        } catch (Exception e) {
            System.err.println("根据URL更新博物馆文件关联失败，博物馆ID: " + museumId + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * 从URL中提取文件ID（临时实现）
     */
    private Long extractFileIdFromUrl(String url) {
        // TODO: 这里需要调用文件服务的API来根据URL查找文件ID
        // 暂时返回null，让系统使用fileIds字段
        return null;
    }
    
    /**
     * 填充博物馆图片URL信息
     */
    public void fillImageUrls(MuseumResponse museum) {
        try {
            // 获取博物馆的图片文件ID列表
            List<Long> fileIds = fileBusinessRelationService.getBusinessFileIds(
                museum.getId(), 
                BusinessTypeEnum.MUSEUM, 
                RelationTypeEnum.GALLERY
            );
            
            if (!CollectionUtils.isEmpty(fileIds)) {
                System.out.println("博物馆ID: " + museum.getId() + ", 找到文件ID: " + fileIds);
                
                // 调用文件服务批量获取URL
                Map<String, Object> response = fileApiClient.getBatchFileUrls(fileIds);
                System.out.println("Feign调用响应: " + response);
                
                if (response != null && response.get("data") != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> fileInfos = (List<Map<String, Object>>) response.get("data");
                    
                    List<String> imageUrls = fileInfos.stream()
                        .map(fileInfo -> (String) fileInfo.get("url"))
                        .filter(url -> url != null && !url.isEmpty())
                        .collect(Collectors.toList());
                    
                    System.out.println("解析出的图片URL: " + imageUrls);
                    museum.setImageUrls(imageUrls);
                    // 同时设置文件ID列表
                    museum.setImageFileIds(fileIds);
                } else {
                    System.out.println("Feign调用响应为空或data为空");
                    museum.setImageUrls(new ArrayList<>());
                    museum.setImageFileIds(new ArrayList<>());
                }
            } else {
                museum.setImageUrls(new ArrayList<>());
                museum.setImageFileIds(new ArrayList<>());
            }
        } catch (Exception e) {
            // 获取图片失败时设置空列表，不影响主要业务
            museum.setImageUrls(new ArrayList<>());
            museum.setImageFileIds(new ArrayList<>());
            System.err.println("获取博物馆图片失败，博物馆ID: " + museum.getId() + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
        
        // 处理Logo URL
        fillLogoUrl(museum);
    }
    
    /**
     * 批量填充博物馆图片URL信息（用于列表场景，避免N+1查询）
     */
    public void batchFillImageUrls(List<MuseumResponse> museums) {
        if (CollectionUtils.isEmpty(museums)) {
            return;
        }
        
        try {
            // 1. 收集所有博物馆ID
            List<Long> museumIds = museums.stream()
                .map(MuseumResponse::getId)
                .collect(Collectors.toList());
            
            // 2. 批量获取所有博物馆的图片文件关系
            Map<Long, List<Long>> museumFileMap = fileBusinessRelationService.getBatchBusinessFileIds(
                museumIds, 
                BusinessTypeEnum.MUSEUM, 
                RelationTypeEnum.GALLERY
            );
            
            // 3. 收集每个博物馆的第一个文件ID（列表只显示第一张图片）
            List<Long> firstFileIds = museumFileMap.values().stream()
                .filter(fileIds -> !fileIds.isEmpty())
                .map(fileIds -> fileIds.get(0)) // 只取第一个文件ID
                .distinct()
                .collect(Collectors.toList());
            
            if (!CollectionUtils.isEmpty(firstFileIds)) {
                // 4. 批量获取文件URL
                Map<String, Object> response = fileApiClient.getBatchFileUrls(firstFileIds);
                
                if (response != null && response.get("data") != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> fileInfos = (List<Map<String, Object>>) response.get("data");
                    
                    // 5. 构建文件ID到URL的映射
                    Map<Long, String> fileIdToUrlMap = fileInfos.stream()
                        .filter(fileInfo -> fileInfo.get("fileId") != null && fileInfo.get("url") != null)
                        .collect(Collectors.toMap(
                            fileInfo -> Long.valueOf(fileInfo.get("fileId").toString()),
                            fileInfo -> (String) fileInfo.get("url"),
                            (existing, replacement) -> existing // 如果有重复key，保留第一个
                        ));
                    
                    // 6. 为每个博物馆设置第一张图片URL
                    for (MuseumResponse museum : museums) {
                        List<Long> fileIds = museumFileMap.get(museum.getId());
                        if (!CollectionUtils.isEmpty(fileIds)) {
                            Long firstFileId = fileIds.get(0);
                            String firstImageUrl = fileIdToUrlMap.get(firstFileId);
                            
                            if (firstImageUrl != null) {
                                museum.setImageUrls(List.of(firstImageUrl)); // 只设置第一张图片
                                museum.setImageFileIds(List.of(firstFileId));
                            } else {
                                museum.setImageUrls(new ArrayList<>());
                                museum.setImageFileIds(new ArrayList<>());
                            }
                        } else {
                            museum.setImageUrls(new ArrayList<>());
                            museum.setImageFileIds(new ArrayList<>());
                        }
                    }
                } else {
                    // 文件服务返回空，为所有博物馆设置空列表
                    museums.forEach(museum -> {
                        museum.setImageUrls(new ArrayList<>());
                        museum.setImageFileIds(new ArrayList<>());
                    });
                }
            } else {
                // 没有文件ID，为所有博物馆设置空列表
                museums.forEach(museum -> {
                    museum.setImageUrls(new ArrayList<>());
                    museum.setImageFileIds(new ArrayList<>());
                });
            }
        } catch (Exception e) {
            log.error("批量获取博物馆图片失败", e);
            // 失败时为所有博物馆设置空列表
            museums.forEach(museum -> {
                museum.setImageUrls(new ArrayList<>());
                museum.setImageFileIds(new ArrayList<>());
            });
        }
    }
    
    /**
     * 填充博物馆Logo URL
     */
    private void fillLogoUrl(MuseumResponse museum) {
        try {
            // 获取Logo文件ID
            List<Long> logoFileIds = fileBusinessRelationService.getBusinessFileIds(
                museum.getId(), 
                BusinessTypeEnum.MUSEUM, 
                RelationTypeEnum.LOGO
            );
            
            System.out.println("博物馆ID: " + museum.getId() + ", Logo文件ID列表: " + logoFileIds);
            
            Long logoFileId = CollectionUtils.isEmpty(logoFileIds) ? null : logoFileIds.get(0);
            
            if (logoFileId != null) {
                museum.setLogoFileId(logoFileId);
                
                // 获取Logo URL
                Map<String, Object> logoResponse = fileApiClient.getFileUrl(logoFileId);
                System.out.println("Logo文件ID: " + logoFileId + ", Feign调用响应: " + logoResponse);
                
                if (logoResponse != null && logoResponse.get("data") != null) {
                    String logoUrl = (String) logoResponse.get("data");
                    museum.setLogoUrl(logoUrl);
                    System.out.println("设置Logo URL: " + logoUrl);
                } else {
                    System.out.println("Logo URL响应为空或data为空");
                }
                // 如果获取失败，logoUrl 保持为 null，由前端处理默认显示
            } else {
                System.out.println("博物馆ID: " + museum.getId() + " 没有Logo关联");
            }
            // 如果没有 logoFileId，logoUrl 保持为 null，由前端处理默认显示
        } catch (Exception e) {
            System.err.println("获取博物馆Logo失败，博物馆ID: " + museum.getId() + ", 错误: " + e.getMessage());
            e.printStackTrace();
            // 异常时不设置 logoUrl，保持为 null，由前端处理默认显示
        }
    }
    
}
