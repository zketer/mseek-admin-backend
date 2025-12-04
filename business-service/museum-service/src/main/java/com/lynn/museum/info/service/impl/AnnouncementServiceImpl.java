package com.lynn.museum.info.service.impl;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.info.dto.AnnouncementCreateRequest;
import com.lynn.museum.info.dto.AnnouncementQueryRequest;
import com.lynn.museum.info.dto.AnnouncementResponse;
import com.lynn.museum.info.mapper.AnnouncementMapper;
import com.lynn.museum.info.model.entity.Announcement;
import com.lynn.museum.info.service.AnnouncementService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.Date;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 公告服务实现
 */
@Service
@Slf4j
public class AnnouncementServiceImpl implements AnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public IPage<AnnouncementResponse> getAnnouncementList(AnnouncementQueryRequest request) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(request.getTitle()), Announcement::getTitle, request.getTitle())
                   .eq(StringUtils.hasText(request.getType()), Announcement::getType, request.getType())
                   .eq(request.getPriority() != null, Announcement::getPriority, request.getPriority())
                   .eq(request.getStatus() != null, Announcement::getStatus, request.getStatus())
                   .ge(request.getStartTime() != null, Announcement::getCreateAt, request.getStartTime())
                   .le(request.getEndTime() != null, Announcement::getCreateAt, request.getEndTime())
                   .orderByDesc(Announcement::getPriority)
                   .orderByDesc(Announcement::getCreateAt);

        Page<Announcement> page = new Page<>(request.getCurrent(), request.getSize());
        Page<Announcement> result = announcementMapper.selectPage(page, queryWrapper);

        return result.convert(this::convertToResponse);
    }

    @Override
    public AnnouncementResponse getAnnouncementDetail(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BizException("公告不存在");
        }
        return convertToResponse(announcement);
    }

    @Override
    public Long createAnnouncement(AnnouncementCreateRequest request) {
        Announcement announcement = new Announcement();
        BeanUtils.copyProperties(request, announcement);
        
        // 时间戳转换为Date
        if (request.getPublishTime() != null) {
            announcement.setPublishTime(new Date(request.getPublishTime()));
        } else if (announcement.getStatus() == 1) {
            // 如果是发布状态但没有指定发布时间，使用当前时间
            announcement.setPublishTime(new Date());
        }
        
        if (request.getExpireTime() != null) {
            announcement.setExpireTime(new Date(request.getExpireTime()));
        }

        announcementMapper.insert(announcement);
        return announcement.getId();
    }

    @Override
    public boolean updateAnnouncement(Long id, AnnouncementCreateRequest request) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BizException("公告不存在");
        }

        // 手动设置字段，避免 BeanUtils.copyProperties 不复制 null 值的问题
        announcement.setTitle(request.getTitle());
        announcement.setContent(request.getContent());
        announcement.setType(request.getType());
        announcement.setPriority(request.getPriority());
        announcement.setStatus(request.getStatus());
        announcement.setEnabled(request.getEnabled());
        
        // 时间戳转换为Date - 明确处理 null 值
        if (request.getPublishTime() != null) {
            announcement.setPublishTime(new Date(request.getPublishTime()));
        } else {
            announcement.setPublishTime(null);
        }
        
        // 处理过期时间：明确设置，包括 null 值
        if (request.getExpireTime() != null) {
            announcement.setExpireTime(new Date(request.getExpireTime()));
        } else {
            announcement.setExpireTime(null);
        }

        // 使用 alwaysUpdateSomeColumnById 或配置实体类字段策略
        return announcementMapper.updateById(announcement) > 0;
    }

    @Override
    public boolean deleteAnnouncement(Long id) {
        // 使用 MyBatis-Plus 的逻辑删除，会自动设置 deleted=1
        // selectById 会自动过滤已删除的记录
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new BizException("公告不存在");
        }

        // 使用 deleteById 会触发逻辑删除，自动将 deleted 设置为 1
        return announcementMapper.deleteById(id) > 0;
    }

    @Override
    public boolean updateStatus(Long id, Integer status) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            throw new BizException("公告不存在");
        }

        announcement.setStatus(status);
        
        // 如果是发布状态且没有发布时间，则设置发布时间
        if (status == 1 && announcement.getPublishTime() == null) {
            announcement.setPublishTime(new Date());
        }

        return announcementMapper.updateById(announcement) > 0;
    }

    @Override
    public boolean publishAnnouncement(Long id) {
        return updateStatus(id, 1);
    }

    @Override
    public boolean offlineAnnouncement(Long id) {
        return updateStatus(id, 2);
    }

    @Override
    public List<AnnouncementResponse> getActiveAnnouncements(Integer limit) {
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Announcement::getStatus, 1)
                   .le(Announcement::getPublishTime, new Date())
                   .and(wrapper -> wrapper.isNull(Announcement::getExpireTime)
                       .or().gt(Announcement::getExpireTime, new Date()))
                   .orderByDesc(Announcement::getPriority)
                   .orderByDesc(Announcement::getPublishTime);

        if (limit != null && limit > 0) {
            queryWrapper.last("LIMIT " + limit);
        }

        List<Announcement> announcements = announcementMapper.selectList(queryWrapper);
        return announcements.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateEnabled(Long id, Integer enabled) {
        // 验证公告是否存在
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            log.warn("公告不存在或已删除，ID：{}", id);
            return false;
        }
        
        // 业务规则：只有已发布状态(status=1)的公告才能切换启用状态
        if (announcement.getStatus() != 1) {
            log.warn("只有已发布状态的公告才能切换启用状态，当前状态：{}，ID：{}", announcement.getStatus(), id);
            return false;
        }
        
        // 更新启用状态
        announcement.setEnabled(enabled);
        announcement.setUpdateAt(new Date());
        
        int updatedRows = announcementMapper.updateById(announcement);
        boolean success = updatedRows > 0;
        if (success) {
            log.info("更新公告状态: id={}, enabled={}", id, enabled);
        }
        return success;
    }

    @Override
    public IPage<AnnouncementResponse> getEnabledAnnouncementList(AnnouncementQueryRequest request) {
        Page<Announcement> page = new Page<>(request.getCurrent(), request.getSize());
        
        LambdaQueryWrapper<Announcement> queryWrapper = new LambdaQueryWrapper<>();
        
        // 基础条件：已发布 + 已启用
        queryWrapper.eq(Announcement::getStatus, 1)
                   // 已启用
                   .eq(Announcement::getEnabled, 1);
        
        // 标题模糊查询
        if (StringUtils.hasText(request.getTitle())) {
            queryWrapper.like(Announcement::getTitle, request.getTitle());
        }
        
        // 类型精确查询
        if (StringUtils.hasText(request.getType())) {
            queryWrapper.eq(Announcement::getType, request.getType());
        }
        
        // 优先级查询
        if (request.getPriority() != null) {
            queryWrapper.eq(Announcement::getPriority, request.getPriority());
        }
        
        // 时间范围查询
        if (request.getStartTime() != null) {
            queryWrapper.ge(Announcement::getPublishTime, request.getStartTime());
        }
        if (request.getEndTime() != null) {
            queryWrapper.le(Announcement::getPublishTime, request.getEndTime());
        }
        
        // 排序：优先级降序，发布时间降序
        queryWrapper.orderByDesc(Announcement::getPriority)
                   .orderByDesc(Announcement::getPublishTime);
        
        IPage<Announcement> announcementPage = announcementMapper.selectPage(page, queryWrapper);
        
        // 转换为响应DTO
        IPage<AnnouncementResponse> responsePage = announcementPage.convert(this::convertToResponse);
        return responsePage;
    }

    @Override
    public boolean incrementReadCount(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null || announcement.getDeleted() == 1) {
            return false;
        }

        announcement.setReadCount(announcement.getReadCount() + 1);
        return announcementMapper.updateById(announcement) > 0;
    }

    private AnnouncementResponse convertToResponse(Announcement announcement) {
        AnnouncementResponse response = new AnnouncementResponse();
        BeanUtils.copyProperties(announcement, response);
        
        // 时间戳转换
        if (announcement.getPublishTime() != null) {
            response.setPublishTime(announcement.getPublishTime().getTime());
        }
        if (announcement.getExpireTime() != null) {
            response.setExpireTime(announcement.getExpireTime().getTime());
        }
        
        return response;
    }
}
