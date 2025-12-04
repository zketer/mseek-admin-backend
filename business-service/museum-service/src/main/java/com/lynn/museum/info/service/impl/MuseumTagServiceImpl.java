package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.common.result.ResultCode;
import com.lynn.museum.info.dto.TagCreateRequest;
import com.lynn.museum.info.dto.TagQueryRequest;
import com.lynn.museum.info.dto.TagResponse;
import com.lynn.museum.info.dto.TagUpdateRequest;
import com.lynn.museum.info.mapper.MuseumTagMapper;
import com.lynn.museum.info.mapper.MuseumTagRelationMapper;
import com.lynn.museum.info.model.entity.MuseumTag;
import com.lynn.museum.info.model.entity.MuseumTagRelation;
import com.lynn.museum.info.service.MuseumTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 博物馆标签服务实现类
 *
 * @author lynn
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class MuseumTagServiceImpl extends ServiceImpl<MuseumTagMapper, MuseumTag> implements MuseumTagService {

    private final MuseumTagRelationMapper museumTagRelationMapper;

    @Override
    public IPage<TagResponse> getTagPage(TagQueryRequest query) {
        LambdaQueryWrapper<MuseumTag> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(MuseumTag::getName, query.getName());
        }
        
        // 排序
        wrapper.orderByDesc(MuseumTag::getCreateAt);
        
        // 分页查询
        Page<MuseumTag> page = new Page<>(query.getPage(), query.getSize());
        Page<MuseumTag> tagPage = page(page, wrapper);
        
        // 转换为响应对象
        IPage<TagResponse> responsePage = tagPage.convert(tag -> {
            TagResponse response = new TagResponse();
            BeanUtils.copyProperties(tag, response);
            return response;
        });
        
        return responsePage;
    }

    @Override
    @Cacheable(value = "museum_tag", key = "'tags'")
    public List<TagResponse> getAllTags() {
        LambdaQueryWrapper<MuseumTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(MuseumTag::getCreateAt);
        
        List<MuseumTag> tags = list(wrapper);
        
        return tags.stream()
                .map(tag -> {
                    TagResponse response = new TagResponse();
                    BeanUtils.copyProperties(tag, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "museum_tag", key = "'tag:' + #id", unless = "#result == null")
    public TagResponse getTagById(Long id) {
        MuseumTag tag = getById(id);
        if (tag == null) {
            throw new BizException(ResultCode.MUSEUM_TAG_NOT_FOUND);
        }
        
        TagResponse response = new TagResponse();
        BeanUtils.copyProperties(tag, response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "museum_tag", key = "'tags'")
    public Long createTag(TagCreateRequest request) {
        // 检查编码是否存在
        if (existsByCode(request.getCode(), null)) {
            throw new BizException(ResultCode.MUSEUM_TAG_CODE_EXISTS);
        }
        
        // 创建标签
        MuseumTag tag = new MuseumTag();
        BeanUtils.copyProperties(request, tag);
        save(tag);
        
        return tag.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "museum_tag", allEntries = true)
    public void updateTag(TagUpdateRequest request) {
        // 检查标签是否存在
        MuseumTag tag = getById(request.getId());
        if (tag == null) {
            throw new BizException(ResultCode.MUSEUM_TAG_NOT_FOUND);
        }
        
        // 更新标签信息
        BeanUtils.copyProperties(request, tag);
        updateById(tag);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "museum_tag", allEntries = true)
    public void deleteTag(Long id) {
        // 检查标签是否存在
        MuseumTag tag = getById(id);
        if (tag == null) {
            throw new BizException(ResultCode.MUSEUM_TAG_NOT_FOUND);
        }
        
        // 检查是否有博物馆关联该标签
        LambdaQueryWrapper<MuseumTagRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MuseumTagRelation::getTagId, id);
        long count = museumTagRelationMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException(ResultCode.MUSEUM_TAG_RELATION_EXISTS);
        }
        
        // 删除标签
        removeById(id);
    }

    @Override
    public boolean existsByCode(String code, Long excludeId) {
        LambdaQueryWrapper<MuseumTag> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MuseumTag::getCode, code);
        if (excludeId != null) {
            wrapper.ne(MuseumTag::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
