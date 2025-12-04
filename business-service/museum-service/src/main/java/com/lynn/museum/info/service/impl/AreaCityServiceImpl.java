package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.*;
import com.lynn.museum.info.mapper.AreaCityMapper;
import com.lynn.museum.info.mapper.AreaProvinceMapper;
import com.lynn.museum.info.model.entity.AreaCity;
import com.lynn.museum.info.model.entity.AreaProvince;
import com.lynn.museum.info.service.AreaCityService;
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
 * 城市管理服务实现
 *
 * @author lynn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AreaCityServiceImpl implements AreaCityService {

    private final AreaCityMapper cityMapper;
    private final AreaProvinceMapper provinceMapper;

    @Override
    public IPage<CityResponse> getCityList(CityQueryRequest request) {
        LambdaQueryWrapper<AreaCity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.hasText(request.getKeyword()), 
                         AreaCity::getName, request.getKeyword())
                   .eq(StringUtils.hasText(request.getAdcode()), 
                      AreaCity::getAdcode, request.getAdcode())
                   .eq(StringUtils.hasText(request.getProvinceAdcode()), 
                      AreaCity::getProvinceAdcode, request.getProvinceAdcode())
                   .eq(StringUtils.hasText(request.getCitycode()), 
                      AreaCity::getCitycode, request.getCitycode())
                   .orderByAsc(AreaCity::getAdcode);

        Page<AreaCity> page = new Page<>(request.getCurrent(), request.getPageSize());
        IPage<AreaCity> result = cityMapper.selectPage(page, queryWrapper);
        
        return result.convert(this::convertToResponse);
    }

    @Override
    @Cacheable(value = "cities", key = "#provinceAdcode")
    public List<CityResponse> getCitiesByProvince(String provinceAdcode) {
        LambdaQueryWrapper<AreaCity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaCity::getProvinceAdcode, provinceAdcode)
                   .orderByAsc(AreaCity::getAdcode);
        List<AreaCity> cities = cityMapper.selectList(queryWrapper);
        return cities.stream().map(this::convertToResponse).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "cities", key = "#id")
    public CityResponse getCityById(Integer id) {
        AreaCity city = cityMapper.selectById(id);
        if (city == null) {
            throw new RuntimeException("城市不存在");
        }
        return convertToResponse(city);
    }

    @Override
    @Cacheable(value = "cities", key = "#adcode")
    public CityResponse getCityByAdcode(String adcode) {
        LambdaQueryWrapper<AreaCity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaCity::getAdcode, adcode);
        AreaCity city = cityMapper.selectOne(queryWrapper);
        if (city == null) {
            throw new RuntimeException("城市不存在");
        }
        return convertToResponse(city);
    }

    @Override
    @CacheEvict(value = "cities", allEntries = true)
    public CityResponse createCity(CityCreateRequest request) {
        log.info("创建城市：{}", request);
        
        // 检查区域代码是否已存在
        LambdaQueryWrapper<AreaCity> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(AreaCity::getAdcode, request.getAdcode());
        if (cityMapper.selectCount(checkWrapper) > 0) {
            throw new RuntimeException("区域代码已存在");
        }
        
        AreaCity city = new AreaCity();
        BeanUtils.copyProperties(request, city);
        
        cityMapper.insert(city);
        return convertToResponse(city);
    }

    @Override
    @CacheEvict(value = "cities", allEntries = true)
    public CityResponse updateCity(Integer id, CityUpdateRequest request) {
        log.info("更新城市：id={}, request={}", id, request);
        
        AreaCity city = cityMapper.selectById(id);
        if (city == null) {
            throw new RuntimeException("城市不存在");
        }
        
        BeanUtils.copyProperties(request, city);
        cityMapper.updateById(city);
        
        return convertToResponse(city);
    }

    @Override
    @CacheEvict(value = "cities", allEntries = true)
    public void deleteCity(Integer id) {
        log.info("删除城市：{}", id);
        
        AreaCity city = cityMapper.selectById(id);
        if (city == null) {
            throw new RuntimeException("城市不存在");
        }
        
        cityMapper.deleteById(id);
    }

    @Override
    @CacheEvict(value = "cities", allEntries = true)
    public void deleteCities(List<Integer> ids) {
        log.info("批量删除城市：{}", ids);
        cityMapper.deleteBatchIds(ids);
    }

    /**
     * 转换为响应DTO
     */
    private CityResponse convertToResponse(AreaCity city) {
        CityResponse response = new CityResponse();
        BeanUtils.copyProperties(city, response);
        
        // 明确设置ID字段，确保不会丢失
        response.setId(city.getId());
        
        // 查询省份名称
        if (StringUtils.hasText(city.getProvinceAdcode())) {
            LambdaQueryWrapper<AreaProvince> provinceQuery = new LambdaQueryWrapper<>();
            provinceQuery.eq(AreaProvince::getAdcode, city.getProvinceAdcode());
            AreaProvince province = provinceMapper.selectOne(provinceQuery);
            if (province != null) {
                response.setProvinceName(province.getName());
            }
        }
        
        return response;
    }
}
