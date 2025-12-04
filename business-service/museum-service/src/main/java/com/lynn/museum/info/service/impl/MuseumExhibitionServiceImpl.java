package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lynn.museum.api.file.client.FileApiClient;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.common.result.ResultCode;
import com.lynn.museum.info.dto.ExhibitionCreateRequest;
import com.lynn.museum.info.dto.ExhibitionQueryRequest;
import com.lynn.museum.info.dto.ExhibitionResponse;
import com.lynn.museum.info.dto.ExhibitionUpdateRequest;
import com.lynn.museum.info.enums.BusinessTypeEnum;
import com.lynn.museum.info.enums.RelationTypeEnum;
import com.lynn.museum.info.mapper.MuseumExhibitionMapper;
import com.lynn.museum.info.mapper.MuseumInfoMapper;
import com.lynn.museum.info.model.entity.MuseumExhibition;
import com.lynn.museum.info.model.entity.MuseumInfo;
import com.lynn.museum.info.service.FileBusinessRelationService;
import com.lynn.museum.info.service.MuseumExhibitionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 博物馆展览服务实现类
 *
 * @author lynn
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class MuseumExhibitionServiceImpl extends ServiceImpl<MuseumExhibitionMapper, MuseumExhibition> implements MuseumExhibitionService {

    private final MuseumExhibitionMapper museumExhibitionMapper;
    private final MuseumInfoMapper museumInfoMapper;
    private final FileBusinessRelationService fileBusinessRelationService;
    private final FileApiClient fileApiClient;

    @Override
    public IPage<ExhibitionResponse> getExhibitionPage(ExhibitionQueryRequest query) {
        Page<MuseumExhibition> page = new Page<>(query.getPage(), query.getSize());
        IPage<ExhibitionResponse> result = museumExhibitionMapper.selectExhibitionPage(page, query);
        
        // 为每个展览填充图片URL信息
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            result.getRecords().forEach(this::fillImageUrls);
        }
        
        return result;
    }

    @Override
    @Cacheable(value = "museum_exhibition", key = "'exhibition:' + #id", unless = "#result == null")
    public ExhibitionResponse getExhibitionById(Long id) {
        ExhibitionResponse exhibition = museumExhibitionMapper.selectExhibitionById(id);
        if (exhibition == null) {
            throw new BizException(ResultCode.MUSEUM_EXHIBITION_NOT_AVAILABLE);
        }
        
        // 填充图片URL信息
        fillImageUrls(exhibition);
        
        return exhibition;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createExhibition(ExhibitionCreateRequest request) {
        // 检查博物馆是否存在
        MuseumInfo museum = museumInfoMapper.selectById(request.getMuseumId());
        if (museum == null) {
            throw new BizException(ResultCode.MUSEUM_NOT_FOUND);
        }
        
        // 创建展览
        MuseumExhibition exhibition = new MuseumExhibition();
        BeanUtils.copyProperties(request, exhibition);
        baseMapper.insert(exhibition);
        
        // 保存文件关联
        saveFileRelations(exhibition.getId(), request.getFileIds());
        
        return exhibition.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "museum_exhibition", key = "'exhibition:' + #request.id")
    public void updateExhibition(ExhibitionUpdateRequest request) {
        // 检查展览是否存在
        MuseumExhibition exhibition = getById(request.getId());
        if (exhibition == null) {
            throw new BizException(ResultCode.MUSEUM_EXHIBITION_NOT_AVAILABLE);
        }
        
        // 检查博物馆是否存在
        MuseumInfo museum = museumInfoMapper.selectById(request.getMuseumId());
        if (museum == null) {
            throw new BizException(ResultCode.MUSEUM_NOT_FOUND);
        }
        
        // 更新展览信息
        BeanUtils.copyProperties(request, exhibition);
        updateById(exhibition);
        
        // 更新文件关联
        updateFileRelations(request.getId(), request.getFileIds());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "museum_exhibition", key = "'exhibition:' + #id")
    public void deleteExhibition(Long id) {
        // 检查展览是否存在
        MuseumExhibition exhibition = getById(id);
        if (exhibition == null) {
            throw new BizException(ResultCode.MUSEUM_EXHIBITION_NOT_AVAILABLE);
        }
        
        // 删除展览
        removeById(id);
    }

    @Override
    @CacheEvict(value = "museum_exhibition", key = "'exhibition:' + #id")
    public void updateStatus(Long id, Integer status) {
        // 检查展览是否存在
        MuseumExhibition exhibition = getById(id);
        if (exhibition == null) {
            throw new BizException(ResultCode.MUSEUM_EXHIBITION_NOT_AVAILABLE);
        }
        
        // 更新状态
        exhibition.setStatus(status);
        updateById(exhibition);
    }

    @Override
    @Cacheable(value = "museum_exhibition", key = "'latest:' + #page + ':' + #pageSize", unless = "#result == null || #result.records.isEmpty()")
    public IPage<ExhibitionResponse> getLatestExhibitions(Integer page, Integer pageSize) {
        Page<ExhibitionResponse> pageRequest = new Page<>(page, pageSize);
        IPage<ExhibitionResponse> result = museumExhibitionMapper.selectLatestExhibitions(pageRequest);
        
        // 填充图片URL
        if (result != null && !CollectionUtils.isEmpty(result.getRecords())) {
            System.out.println("🖼️ 开始为最新展览填充图片URL，数量：" + result.getRecords().size());
            for (ExhibitionResponse exhibition : result.getRecords()) {
                fillImageUrls(exhibition);
            }
            System.out.println("🖼️ 最新展览图片URL填充完成");
        }
        
        return result;
    }

    @Override
    @Cacheable(value = "museum_exhibition", key = "'all:' + #page + ':' + #pageSize + ':' + #museumId + ':' + #title + ':' + #status + ':' + #isPermanent", unless = "#result == null || #result.records.isEmpty()")
    public IPage<ExhibitionResponse> getAllExhibitions(Integer page, Integer pageSize, Long museumId, String title, Integer status, Integer isPermanent) {
        Page<MuseumExhibition> pageRequest = new Page<>(page, pageSize);
        IPage<ExhibitionResponse> result = museumExhibitionMapper.selectAllExhibitions(pageRequest, museumId, title, status, isPermanent);
        
        // 填充图片URL
        if (result != null && !CollectionUtils.isEmpty(result.getRecords())) {
            System.out.println("🖼️ 开始为所有展览填充图片URL，数量：" + result.getRecords().size());
            for (ExhibitionResponse exhibition : result.getRecords()) {
                fillImageUrls(exhibition);
            }
            System.out.println("🖼️ 所有展览图片URL填充完成");
        }
        
        return result;
    }

    /**
     * 填充展览图片URL信息
     */
    private void fillImageUrls(ExhibitionResponse exhibition) {
        try {
            // 获取展览的图片文件ID列表
            List<Long> fileIds = fileBusinessRelationService.getBusinessFileIds(
                exhibition.getId(),
                BusinessTypeEnum.EXHIBITION,
                RelationTypeEnum.GALLERY
            );

            if (!CollectionUtils.isEmpty(fileIds)) {
                System.out.println("展览ID: " + exhibition.getId() + ", 找到文件ID: " + fileIds);

                // 调用文件服务批量获取URL
                Map<String, Object> response = fileApiClient.getBatchFileUrls(fileIds);
                System.out.println("Feign调用响应: " + response);

                if (response != null && response.get("data") != null) {
                    @SuppressWarnings("unchecked")
                    List<Map<String, Object>> fileInfos = (List<Map<String, Object>>) response.get("data");

                    // 解析出图片URL
                    List<String> imageUrls = fileInfos.stream()
                        .map(fileInfo -> (String) fileInfo.get("url"))
                        .filter(url -> url != null && !url.isEmpty())
                        .collect(Collectors.toList());

                    System.out.println("解析出的图片URL: " + imageUrls);
                    // 同时设置文件ID列表
                    exhibition.setImageFileIds(fileIds);
                    exhibition.setImageUrls(imageUrls);
                } else {
                    System.out.println("Feign调用响应为空或data为空");
                }
            }
        } catch (Exception e) {
            System.err.println("填充展览图片URL失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 保存文件关联关系
     */
    private void saveFileRelations(Long exhibitionId, List<Long> fileIds) {
        if (CollectionUtils.isEmpty(fileIds)) {
            return;
        }
        
        try {
            // 为展览批量创建图片关联
            fileBusinessRelationService.batchCreateRelation(
                fileIds, 
                exhibitionId, 
                BusinessTypeEnum.EXHIBITION,
                RelationTypeEnum.GALLERY,
                    // 创建者ID
                1L
            );
        } catch (Exception e) {
            System.err.println("展览文件关联创建失败，展览ID: " + exhibitionId + ", 文件ID: " + fileIds + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 更新文件关联关系
     */
    private void updateFileRelations(Long exhibitionId, List<Long> fileIds) {
        try {
            // 删除现有的所有图片关联
            fileBusinessRelationService.deleteByBusinessAndRelation(
                exhibitionId,
                BusinessTypeEnum.EXHIBITION,
                RelationTypeEnum.GALLERY
            );

            // 重新创建前端传入的所有关联
            if (!CollectionUtils.isEmpty(fileIds)) {
                fileBusinessRelationService.batchCreateRelation(
                    fileIds,
                    exhibitionId,
                    BusinessTypeEnum.EXHIBITION,
                    RelationTypeEnum.GALLERY,
                        // 创建者ID
                    1L
                );
            }

            System.out.println("展览ID: " + exhibitionId + ", 更新文件关联: " + fileIds);
        } catch (Exception e) {
            System.err.println("展览文件关联更新失败，展览ID: " + exhibitionId + ", 文件ID: " + fileIds + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
