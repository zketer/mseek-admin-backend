package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.info.dto.*;

import java.util.List;

/**
 * 省份管理服务接口
 *
 * @author lynn
 */
public interface AreaProvinceService {

    /**
     * 分页查询省份列表
     *
     * @param request 查询参数
     * @return 分页结果
     */
    IPage<ProvinceResponse> getProvinceList(ProvinceQueryRequest request);

    /**
     * 获取所有省份列表（不分页）
     *
     * @return 省份列表
     */
    List<ProvinceResponse> getAllProvinces();

    /**
     * 根据ID获取省份详情
     *
     * @param id 省份ID
     * @return 省份详情
     */
    ProvinceResponse getProvinceById(Integer id);

    /**
     * 根据区域代码获取省份详情
     *
     * @param adcode 区域代码
     * @return 省份详情
     */
    ProvinceResponse getProvinceByAdcode(String adcode);

    /**
     * 创建省份
     *
     * @param request 创建请求
     * @return 创建结果
     */
    ProvinceResponse createProvince(ProvinceCreateRequest request);

    /**
     * 更新省份
     *
     * @param id 省份ID
     * @param request 更新请求
     * @return 更新结果
     */
    ProvinceResponse updateProvince(Integer id, ProvinceUpdateRequest request);

    /**
     * 删除省份
     *
     * @param id 省份ID
     */
    void deleteProvince(Integer id);

    /**
     * 批量删除省份
     *
     * @param ids 省份ID列表
     */
    void deleteProvinces(List<Integer> ids);
}
