package com.lynn.museum.info.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.info.dto.*;

import java.util.List;

/**
 * 区县管理服务接口
 *
 * @author lynn
 */
public interface AreaDistrictService {

    /**
     * 分页查询区县列表
     */
    IPage<DistrictResponse> getDistrictList(DistrictQueryRequest request);

    /**
     * 根据城市获取区县列表（不分页）
     */
    List<DistrictResponse> getDistrictsByCity(String cityAdcode);

    /**
     * 根据ID获取区县详情
     */
    DistrictResponse getDistrictById(Integer id);

    /**
     * 根据区域代码获取区县详情
     */
    DistrictResponse getDistrictByAdcode(String adcode);

    /**
     * 创建区县
     */
    DistrictResponse createDistrict(DistrictCreateRequest request);

    /**
     * 更新区县
     */
    DistrictResponse updateDistrict(Integer id, DistrictUpdateRequest request);

    /**
     * 删除区县
     */
    void deleteDistrict(Integer id);

    /**
     * 批量删除区县
     */
    void deleteDistricts(List<Integer> ids);
}
