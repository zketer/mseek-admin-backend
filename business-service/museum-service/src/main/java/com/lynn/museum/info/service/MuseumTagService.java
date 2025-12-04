package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lynn.museum.info.dto.TagCreateRequest;
import com.lynn.museum.info.dto.TagQueryRequest;
import com.lynn.museum.info.dto.TagResponse;
import com.lynn.museum.info.dto.TagUpdateRequest;
import com.lynn.museum.info.model.entity.MuseumTag;

import java.util.List;

/**
 * 博物馆标签服务接口
 *
 * @author lynn
 * @since 2024-01-01
 */
public interface MuseumTagService extends IService<MuseumTag> {

    /**
     * 分页查询标签列表
     *
     * @param query 查询条件
     * @return 分页结果
     */
    IPage<TagResponse> getTagPage(TagQueryRequest query);

    /**
     * 获取所有标签列表
     *
     * @return 标签列表
     */
    List<TagResponse> getAllTags();

    /**
     * 获取标签详情
     *
     * @param id 标签ID
     * @return 标签详情
     */
    TagResponse getTagById(Long id);

    /**
     * 创建标签
     *
     * @param request 创建请求
     * @return 标签ID
     */
    Long createTag(TagCreateRequest request);

    /**
     * 更新标签
     *
     * @param request 更新请求
     */
    void updateTag(TagUpdateRequest request);

    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTag(Long id);

    /**
     * 检查标签编码是否存在
     *
     * @param code      标签编码
     * @param excludeId 排除的标签ID（可选）
     * @return 是否存在
     */
    boolean existsByCode(String code, Long excludeId);
}
