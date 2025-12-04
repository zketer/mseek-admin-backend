package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.StreetCreateRequest;
import com.lynn.museum.info.dto.StreetResponse;
import com.lynn.museum.info.dto.StreetUpdateRequest;
import com.lynn.museum.info.mapper.AreaCityMapper;
import com.lynn.museum.info.mapper.AreaDistrictMapper;
import com.lynn.museum.info.mapper.AreaProvinceMapper;
import com.lynn.museum.info.mapper.AreaStreetMapper;
import com.lynn.museum.info.model.entity.AreaCity;
import com.lynn.museum.info.model.entity.AreaDistrict;
import com.lynn.museum.info.model.entity.AreaProvince;
import com.lynn.museum.info.model.entity.AreaStreet;
import com.lynn.museum.info.service.AreaStreetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 街道服务实现
 *
 * @author lynn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AreaStreetServiceImpl implements AreaStreetService {

    private final AreaStreetMapper streetMapper;
    private final AreaDistrictMapper districtMapper;
    private final AreaCityMapper cityMapper;
    private final AreaProvinceMapper provinceMapper;

    @Override
    @Cacheable(value = "area:streets-page", key = "#current + ':' + #pageSize + ':' + (#keyword != null ? #keyword : 'null') + ':' + (#adcode != null ? #adcode : 'null')")
    public IPage<StreetResponse> getStreetsPage(Integer current, Integer pageSize, String keyword, String adcode) {
        LambdaQueryWrapper<AreaStreet> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加搜索条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(AreaStreet::getName, keyword);
        }
        if (StringUtils.hasText(adcode)) {
            queryWrapper.like(AreaStreet::getAdcode, adcode);
        }
        
        // 排序
        queryWrapper.orderBy(true, true, AreaStreet::getName);
        
        // 分页查询
        Page<AreaStreet> page = new Page<>(current, pageSize);
        Page<AreaStreet> streetPage = streetMapper.selectPage(page, queryWrapper);
        
        // 转换为响应DTO并添加关联信息
        return streetPage.convert(street -> {
            StreetResponse response = new StreetResponse();
            BeanUtils.copyProperties(street, response);
            
            // 明确设置ID字段，确保不会丢失
            response.setId(street.getId());
            
            
            // 查询区县、城市和省份名称
            AreaDistrict district = districtMapper.selectOne(new LambdaQueryWrapper<AreaDistrict>().eq(AreaDistrict::getAdcode, street.getDistrictAdcode()));
            if (district != null) {
                response.setDistrictName(district.getName());
                
                // 查询城市名称
                AreaCity city = cityMapper.selectOne(new LambdaQueryWrapper<AreaCity>().eq(AreaCity::getAdcode, district.getCityAdcode()));
                if (city != null) {
                    response.setCityName(city.getName());
                    response.setCityAdcode(city.getAdcode());
                    
                    // 查询省份名称
                    AreaProvince province = provinceMapper.selectOne(new LambdaQueryWrapper<AreaProvince>().eq(AreaProvince::getAdcode, city.getProvinceAdcode()));
                    if (province != null) {
                        response.setProvinceName(province.getName());
                        response.setProvinceAdcode(province.getAdcode());
                    }
                }
            }
            
            return response;
        });
    }

    @Override
    @Cacheable(value = "area:streets", key = "#districtCode")
    public List<StreetResponse> getStreetsByDistrict(String districtCode) {
        LambdaQueryWrapper<AreaStreet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaStreet::getDistrictAdcode, districtCode)
                   .orderBy(true, true, AreaStreet::getName);
        List<AreaStreet> streets = streetMapper.selectList(queryWrapper);
        return streets.stream().map(this::convertStreetToResponse).collect(Collectors.toList());
    }

    @Override
    public StreetResponse getStreetById(Integer id) {
        AreaStreet street = streetMapper.selectById(id);
        if (street == null) {
            return null;
        }
        return convertStreetToResponse(street);
    }

    @Override
    public StreetResponse getStreetByAdcode(String adcode) {
        LambdaQueryWrapper<AreaStreet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaStreet::getAdcode, adcode);
        AreaStreet street = streetMapper.selectOne(queryWrapper);
        if (street == null) {
            return null;
        }
        return convertStreetToResponse(street);
    }

    @Override
    public StreetResponse createStreet(StreetCreateRequest request) {
        log.info("创建街道: {}", request);
        AreaStreet street = new AreaStreet();
        BeanUtils.copyProperties(request, street);
        streetMapper.insert(street);
        return convertStreetToResponse(street);
    }

    @Override
    public StreetResponse updateStreet(Integer id, StreetUpdateRequest request) {
        log.info("更新街道: id={}, request={}", id, request);
        AreaStreet street = streetMapper.selectById(id);
        if (street == null) {
            return null;
        }
        BeanUtils.copyProperties(request, street);
        streetMapper.updateById(street);
        return convertStreetToResponse(street);
    }

    @Override
    public void deleteStreet(Integer id) {
        log.info("删除街道: {}", id);
        streetMapper.deleteById(id);
    }

    @Override
    public void deleteStreets(List<Integer> ids) {
        log.info("批量删除街道: {}", ids);
        streetMapper.deleteBatchIds(ids);
    }

    /**
     * 转换街道实体为响应DTO
     */
    private StreetResponse convertStreetToResponse(AreaStreet street) {
        StreetResponse response = new StreetResponse();
        BeanUtils.copyProperties(street, response);
        return response;
    }
}
