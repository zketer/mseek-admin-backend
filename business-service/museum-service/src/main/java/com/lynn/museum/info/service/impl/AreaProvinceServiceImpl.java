package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.*;
import com.lynn.museum.info.mapper.AreaProvinceMapper;
import com.lynn.museum.info.model.entity.AreaProvince;
import com.lynn.museum.info.service.AreaProvinceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 省份管理服务实现
 *
 * @author lynn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AreaProvinceServiceImpl implements AreaProvinceService {

    private final AreaProvinceMapper provinceMapper;

    @Override
    public IPage<ProvinceResponse> getProvinceList(ProvinceQueryRequest request) {
        LambdaQueryWrapper<AreaProvince> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(request.getKeyword()), 
                         AreaProvince::getName, request.getKeyword())
                   .eq(StringUtils.hasText(request.getAdcode()), 
                      AreaProvince::getAdcode, request.getAdcode())
                   .eq(StringUtils.hasText(request.getCountryAdcode()), 
                      AreaProvince::getCountryAdcode, request.getCountryAdcode())
                   .orderByAsc(AreaProvince::getAdcode);

        Page<AreaProvince> page = new Page<>(request.getCurrent(), request.getPageSize());
        IPage<AreaProvince> result = provinceMapper.selectPage(page, queryWrapper);
        
        return result.convert(this::convertToResponse);
    }

    @Override
    @Cacheable(value = "provinces", key = "'all'")
    public List<ProvinceResponse> getAllProvinces() {
        List<AreaProvince> provinces = provinceMapper.selectList(
            new LambdaQueryWrapper<AreaProvince>().orderByAsc(AreaProvince::getAdcode)
        );
        return provinces.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "provinces", key = "#id")
    public ProvinceResponse getProvinceById(Integer id) {
        AreaProvince province = provinceMapper.selectById(id);
        if (province == null) {
            throw new RuntimeException("省份不存在");
        }
        return convertToResponse(province);
    }

    @Override
    @Cacheable(value = "provinces", key = "#adcode")
    public ProvinceResponse getProvinceByAdcode(String adcode) {
        LambdaQueryWrapper<AreaProvince> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaProvince::getAdcode, adcode);
        AreaProvince province = provinceMapper.selectOne(queryWrapper);
        if (province == null) {
            throw new RuntimeException("省份不存在");
        }
        return convertToResponse(province);
    }

    @Override
    @CacheEvict(value = "provinces", allEntries = true)
    public ProvinceResponse createProvince(ProvinceCreateRequest request) {
        log.info("创建省份：{}", request);
        
        // 检查区域代码是否已存在
        LambdaQueryWrapper<AreaProvince> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(AreaProvince::getAdcode, request.getAdcode());
        if (provinceMapper.selectCount(checkWrapper) > 0) {
            throw new RuntimeException("区域代码已存在");
        }
        
        AreaProvince province = new AreaProvince();
        BeanUtils.copyProperties(request, province);
        
        provinceMapper.insert(province);
        return convertToResponse(province);
    }

    @Override
    @CacheEvict(value = "provinces", allEntries = true)
    public ProvinceResponse updateProvince(Integer id, ProvinceUpdateRequest request) {
        log.info("更新省份：id={}, request={}", id, request);
        
        AreaProvince province = provinceMapper.selectById(id);
        if (province == null) {
            throw new RuntimeException("省份不存在");
        }
        
        BeanUtils.copyProperties(request, province);
        provinceMapper.updateById(province);
        
        return convertToResponse(province);
    }

    @Override
    @CacheEvict(value = "provinces", allEntries = true)
    public void deleteProvince(Integer id) {
        log.info("删除省份：{}", id);
        
        AreaProvince province = provinceMapper.selectById(id);
        if (province == null) {
            throw new RuntimeException("省份不存在");
        }
        
        provinceMapper.deleteById(id);
    }

    @Override
    @CacheEvict(value = "provinces", allEntries = true)
    public void deleteProvinces(List<Integer> ids) {
        log.info("批量删除省份：{}", ids);
        provinceMapper.deleteBatchIds(ids);
    }

    /**
     * 转换为响应DTO
     */
    private ProvinceResponse convertToResponse(AreaProvince province) {
        ProvinceResponse response = new ProvinceResponse();
        BeanUtils.copyProperties(province, response);
        
        // 明确设置ID字段，确保不会丢失
        response.setId(province.getId());
        return response;
    }
}
