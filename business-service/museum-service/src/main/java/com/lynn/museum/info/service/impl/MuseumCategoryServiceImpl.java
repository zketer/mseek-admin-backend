package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.common.result.ResultCode;
import com.lynn.museum.info.dto.CategoryCreateRequest;
import com.lynn.museum.info.dto.CategoryQueryRequest;
import com.lynn.museum.info.dto.CategoryResponse;
import com.lynn.museum.info.dto.CategoryUpdateRequest;
import com.lynn.museum.info.mapper.MuseumCategoryMapper;
import com.lynn.museum.info.mapper.MuseumCategoryRelationMapper;
import com.lynn.museum.info.model.entity.MuseumCategory;
import com.lynn.museum.info.service.MuseumCategoryService;
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
 * 博物馆分类服务实现类
 *
 * @author lynn
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
public class MuseumCategoryServiceImpl extends ServiceImpl<MuseumCategoryMapper, MuseumCategory> implements MuseumCategoryService {

    private final MuseumCategoryRelationMapper museumCategoryRelationMapper;

    @Override
    public IPage<CategoryResponse> getCategoryPage(CategoryQueryRequest query) {
        LambdaQueryWrapper<MuseumCategory> wrapper = new LambdaQueryWrapper<>();
        
        // 添加查询条件
        if (StringUtils.hasText(query.getName())) {
            wrapper.like(MuseumCategory::getName, query.getName());
        }
        if (query.getStatus() != null) {
            wrapper.eq(MuseumCategory::getStatus, query.getStatus());
        }
        
        // 排序
        wrapper.orderByAsc(MuseumCategory::getSortOrder)
               .orderByDesc(MuseumCategory::getCreateAt);
        
        // 分页查询
        Page<MuseumCategory> page = new Page<>(query.getPage(), query.getSize());
        Page<MuseumCategory> categoryPage = page(page, wrapper);
        
        // 转换为响应对象
        IPage<CategoryResponse> responsePage = categoryPage.convert(category -> {
            CategoryResponse response = new CategoryResponse();
            BeanUtils.copyProperties(category, response);
            return response;
        });
        
        return responsePage;
    }

    @Override
    @Cacheable(value = "museum_category", key = "'categories'")
    public List<CategoryResponse> getAllCategories() {
        LambdaQueryWrapper<MuseumCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MuseumCategory::getStatus, 1)
               .orderByAsc(MuseumCategory::getSortOrder)
               .orderByDesc(MuseumCategory::getCreateAt);
        
        List<MuseumCategory> categories = list(wrapper);
        
        return categories.stream()
                .map(category -> {
                    CategoryResponse response = new CategoryResponse();
                    BeanUtils.copyProperties(category, response);
                    return response;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "museum_category", key = "'category:' + #id", unless = "#result == null")
    public CategoryResponse getCategoryById(Long id) {
        MuseumCategory category = getById(id);
        if (category == null) {
            throw new BizException(ResultCode.MUSEUM_CATEGORY_NOT_FOUND);
        }
        
        CategoryResponse response = new CategoryResponse();
        BeanUtils.copyProperties(category, response);
        return response;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "museum_category", key = "'categories'")
    public Long createCategory(CategoryCreateRequest request) {
        // 检查编码是否存在
        if (existsByCode(request.getCode(), null)) {
            throw new BizException(ResultCode.MUSEUM_CATEGORY_CODE_EXISTS);
        }
        
        // 创建分类
        MuseumCategory category = new MuseumCategory();
        BeanUtils.copyProperties(request, category);
        save(category);
        
        return category.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "museum_category", allEntries = true)
    public void updateCategory(CategoryUpdateRequest request) {
        // 检查分类是否存在
        MuseumCategory category = getById(request.getId());
        if (category == null) {
            throw new BizException(ResultCode.MUSEUM_CATEGORY_NOT_FOUND);
        }
        
        // 更新分类信息
        BeanUtils.copyProperties(request, category);
        updateById(category);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheEvict(value = "museum_category", allEntries = true)
    public void deleteCategory(Long id) {
        // 检查分类是否存在
        MuseumCategory category = getById(id);
        if (category == null) {
            throw new BizException(ResultCode.MUSEUM_CATEGORY_NOT_FOUND);
        }
        
        // 检查是否有博物馆关联该分类
        LambdaQueryWrapper<com.lynn.museum.info.model.entity.MuseumCategoryRelation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(com.lynn.museum.info.model.entity.MuseumCategoryRelation::getCategoryId, id);
        long count = museumCategoryRelationMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BizException(ResultCode.MUSEUM_CATEGORY_RELATION_EXISTS);
        }
        
        // 删除分类
        removeById(id);
    }

    @Override
    @CacheEvict(value = "museum_category", allEntries = true)
    public void updateStatus(Long id, Integer status) {
        // 检查分类是否存在
        MuseumCategory category = getById(id);
        if (category == null) {
            throw new BizException(ResultCode.MUSEUM_CATEGORY_NOT_FOUND);
        }
        
        // 更新状态
        category.setStatus(status);
        updateById(category);
    }

    @Override
    public boolean existsByCode(String code, Long excludeId) {
        LambdaQueryWrapper<MuseumCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MuseumCategory::getCode, code);
        if (excludeId != null) {
            wrapper.ne(MuseumCategory::getId, excludeId);
        }
        return count(wrapper) > 0;
    }
}
