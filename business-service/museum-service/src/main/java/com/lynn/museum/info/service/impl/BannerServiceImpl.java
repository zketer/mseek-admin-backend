package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lynn.museum.info.dto.BannerCreateRequest;
import com.lynn.museum.info.dto.BannerQueryRequest;
import com.lynn.museum.info.dto.BannerResponse;
import com.lynn.museum.info.mapper.BannerMapper;
import com.lynn.museum.info.model.entity.Banner;
import com.lynn.museum.info.service.BannerService;
import com.lynn.museum.info.service.FileBusinessRelationService;
import com.lynn.museum.info.enums.BusinessTypeEnum;
import com.lynn.museum.info.enums.RelationTypeEnum;
import com.lynn.museum.api.file.client.FileApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.util.CollectionUtils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 轮播图服务实现
 *
 * @author lynn
 * @since 2024-12-16
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    private final FileBusinessRelationService fileBusinessRelationService;
    private final FileApiClient fileApiClient;

    @Override
    public IPage<BannerResponse> getBanners(BannerQueryRequest query) {
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(query.getTitle()), Banner::getTitle, query.getTitle())
                .eq(StringUtils.hasText(query.getLinkType()), Banner::getLinkType, query.getLinkType())
                .eq(query.getStatus() != null, Banner::getStatus, query.getStatus())
                .orderByDesc(Banner::getSort)
                .orderByDesc(Banner::getCreateAt);

        Page<Banner> page = new Page<>(query.getCurrent(), query.getSize());
        IPage<Banner> bannerPage = page(page, queryWrapper);

        // 转换为响应DTO
        Page<BannerResponse> responsePage = new Page<>(bannerPage.getCurrent(), bannerPage.getSize(), bannerPage.getTotal());
        List<BannerResponse> responseList = bannerPage.getRecords().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        responsePage.setRecords(responseList);

        return responsePage;
    }

    @Override
    @Transactional
    public Long createBanner(BannerCreateRequest request) {
        Banner banner = new Banner();
        BeanUtils.copyProperties(request, banner);
        save(banner);
        
        // 如果有文件ID，创建文件关联关系
        if (request.getFileId() != null) {
            try {
                fileBusinessRelationService.setMainImage(
                    request.getFileId(), 
                    banner.getId(), 
                    BusinessTypeEnum.BANNER, 
                    banner.getCreateBy()
                );
            } catch (Exception e) {
                log.warn("横幅文件关联创建失败，横幅ID: {}, 文件ID: {}, 错误: {}", 
                    banner.getId(), request.getFileId(), e.getMessage());
                // 不阻断横幅创建流程，只记录警告
            }
        }
        
        log.info("创建轮播图: id={}, title={}", banner.getId(), banner.getTitle());
        return banner.getId();
    }

    @Override
    @Transactional
    public void updateBanner(Long id, BannerCreateRequest request) {
        Banner banner = getById(id);
        if (banner == null) {
            throw new RuntimeException("轮播图不存在");
        }

        BeanUtils.copyProperties(request, banner);
        banner.setId(id);
        updateById(banner);
        
        // 处理文件关联更新
        if (request.getFileId() != null) {
            try {
                // 替换横幅的主图文件关联
                fileBusinessRelationService.setMainImage(
                    request.getFileId(), 
                    id, 
                    BusinessTypeEnum.BANNER, 
                    banner.getUpdateBy()
                );
            } catch (Exception e) {
                log.warn("横幅文件关联更新失败，横幅ID: {}, 文件ID: {}, 错误: {}", 
                    id, request.getFileId(), e.getMessage());
            }
        }
        
        log.info("更新轮播图: id={}, title={}", id, banner.getTitle());
    }

    @Override
    @Transactional
    public void deleteBanner(Long id) {
        // 删除横幅的文件关联关系
        try {
            fileBusinessRelationService.deleteByBusiness(id, BusinessTypeEnum.BANNER);
        } catch (Exception e) {
            log.warn("删除横幅文件关联失败，横幅ID: {}, 错误: {}", id, e.getMessage());
            // 继续删除横幅，不因文件关联删除失败而阻断
        }
        
        removeById(id);
        log.info("删除轮播图: id={}", id);
    }

    @Override
    @Transactional
    public void updateBannerStatus(Long id, Integer status) {
        LambdaUpdateWrapper<Banner> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Banner::getId, id)
                .set(Banner::getStatus, status);
        update(updateWrapper);
        
        log.info("更新轮播图状态: id={}, status={}", id, status);
    }

    @Override
    public List<BannerResponse> getActiveBanners(Integer limit) {
        LambdaQueryWrapper<Banner> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Banner::getStatus, 1)
                .and(wrapper -> wrapper
                        .isNull(Banner::getStartTime)
                        .or()
                        .le(Banner::getStartTime, new Date())
                )
                .and(wrapper -> wrapper
                        .isNull(Banner::getEndTime)
                        .or()
                        .ge(Banner::getEndTime, new Date())
                )
                .orderByDesc(Banner::getSort);

        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }

        List<Banner> banners = list(queryWrapper);
        return banners.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean incrementClickCount(Long id) {
        try {
            LambdaUpdateWrapper<Banner> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(Banner::getId, id)
                    .setSql("click_count = click_count + 1");
            boolean success = update(updateWrapper);
            
            return success;
        } catch (Exception e) {
            log.error("记录轮播图点击失败，ID: {}", id, e);
            return false;
        }
    }

    private BannerResponse convertToResponse(Banner banner) {
        BannerResponse response = new BannerResponse();
        BeanUtils.copyProperties(banner, response);
        
        // 获取横幅的图片URL
        fillImageUrl(response);
        
        return response;
    }
    
    /**
     * 填充横幅图片URL信息
     */
    private void fillImageUrl(BannerResponse banner) {
        try {
            // 获取横幅的主图文件ID
            List<Long> fileIds = fileBusinessRelationService.getBusinessFileIds(
                banner.getId(), 
                BusinessTypeEnum.BANNER, 
                RelationTypeEnum.MAIN_IMAGE
            );
            
            if (!CollectionUtils.isEmpty(fileIds)) {
                // 横幅只有一张主图
                Long fileId = fileIds.get(0); 
                System.out.println("横幅ID: " + banner.getId() + ", 找到文件ID: " + fileId);
                
                // 设置fileId到响应对象
                banner.setFileId(fileId);
                
                // 调用文件服务获取URL
                Map<String, Object> response = fileApiClient.getFileUrl(fileId);
                System.out.println("Feign调用响应: " + response);
                
                if (response != null && response.get("data") != null) {
                    String imageUrl = (String) response.get("data");
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        System.out.println("获取到图片URL: " + imageUrl);
                        banner.setImageUrl(imageUrl);
                    }
                } else {
                    System.out.println("Feign调用响应为空或data为空");
                }
            } else {
                System.out.println("横幅ID: " + banner.getId() + ", 未找到关联的文件");
            }
        } catch (Exception e) {
            // 获取图片失败时不影响主要业务，使用原有的imageUrl
            System.err.println("获取横幅图片失败，横幅ID: " + banner.getId() + ", 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
