package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lynn.museum.info.dto.CategoryCreateRequest;
import com.lynn.museum.info.dto.CategoryQueryRequest;
import com.lynn.museum.info.dto.CategoryResponse;
import com.lynn.museum.info.dto.CategoryUpdateRequest;
import com.lynn.museum.info.model.entity.MuseumCategory;

import java.util.List;

/**
 * 博物馆分类服务接口
 *
 * @author lynn
 * @since 2024-01-01
 */
public interface MuseumCategoryService extends IService<MuseumCategory> {

    /**
     * 分页查询分类列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<CategoryResponse> getCategoryPage(CategoryQueryRequest query);

    /**
     * 获取所有分类列表
     *
     * @return 分类列表
     */
    List<CategoryResponse> getAllCategories();

    /**
     * 获取分类详情
     *
     * @param id 分类ID
     * @return 分类详情
     */
    CategoryResponse getCategoryById(Long id);

    /**
     * 创建分类
     *
     * @param request 创建请求
     * @return 分类ID
     */
    Long createCategory(CategoryCreateRequest request);

    /**
     * 更新分类
     *
     * @param request 更新请求
     */
    void updateCategory(CategoryUpdateRequest request);

    /**
     * 删除分类
     *
     * @param id 分类ID
     */
    void deleteCategory(Long id);

    /**
     * 更新分类状态
     *
     * @param id     分类ID
     * @param status 状态：0-禁用，1-启用
     */
    void updateStatus(Long id, Integer status);

    /**
     * 检查分类编码是否存在
     *
     * @param code      分类编码
     * @param excludeId 排除的分类ID（可选）
     * @return 是否存在
     */
    boolean existsByCode(String code, Long excludeId);
}
