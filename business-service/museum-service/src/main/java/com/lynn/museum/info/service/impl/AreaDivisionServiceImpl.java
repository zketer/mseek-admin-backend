package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.AreaDivisionResponse;
import com.lynn.museum.info.dto.DistrictCreateRequest;
import com.lynn.museum.info.dto.DistrictResponse;
import com.lynn.museum.info.dto.DistrictUpdateRequest;
import com.lynn.museum.info.dto.StreetResponse;
import com.lynn.museum.info.mapper.AreaCityMapper;
import com.lynn.museum.info.mapper.AreaDistrictMapper;
import com.lynn.museum.info.mapper.AreaProvinceMapper;
import com.lynn.museum.info.mapper.AreaStreetMapper;
import com.lynn.museum.info.model.entity.AreaCity;
import com.lynn.museum.info.model.entity.AreaDistrict;
import com.lynn.museum.info.model.entity.AreaProvince;
import com.lynn.museum.info.model.entity.AreaStreet;
import com.lynn.museum.info.service.AreaDivisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 区域行政区划服务实现
 *
 * @author lynn
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AreaDivisionServiceImpl implements AreaDivisionService {

    private final AreaProvinceMapper provinceMapper;
    private final AreaCityMapper cityMapper;
    private final AreaDistrictMapper districtMapper;
    private final AreaStreetMapper streetMapper;

    @Override
    @Cacheable(value = "area:provinces", key = "'all'")
    public List<AreaDivisionResponse> getAllProvinces() {
        List<AreaProvince> provinces = provinceMapper.selectList(null);
        return provinces.stream().map(this::convertProvinceToResponse).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "area:cities", key = "#provinceCode")
    public List<AreaDivisionResponse> getCitiesByProvince(String provinceCode) {
        LambdaQueryWrapper<AreaCity> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaCity::getProvinceAdcode, provinceCode);
        List<AreaCity> cities = cityMapper.selectList(queryWrapper);
        return cities.stream().map(this::convertCityToResponse).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "area:districts", key = "#cityCode")
    public List<AreaDivisionResponse> getDistrictsByCity(String cityCode) {
        LambdaQueryWrapper<AreaDistrict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaDistrict::getCityAdcode, cityCode);
        List<AreaDistrict> districts = districtMapper.selectList(queryWrapper);
        return districts.stream().map(this::convertDistrictToResponse).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "area:tree", key = "'full'")
    public List<AreaDivisionResponse> getAreaDivisionTree() {
        List<AreaDivisionResponse> provinces = getAllProvinces();
        
        for (AreaDivisionResponse province : provinces) {
            List<AreaDivisionResponse> cities = getCitiesByProvince(province.getAdcode());
            for (AreaDivisionResponse city : cities) {
                List<AreaDivisionResponse> districts = getDistrictsByCity(city.getAdcode());
                city.setChildren(districts);
            }
            province.setChildren(cities);
        }
        
        return provinces;
    }

    @Override
    @Cacheable(value = "area:division", key = "#adcode")
    public AreaDivisionResponse getByAdcode(String adcode) {
        // 先尝试从省份查找
        LambdaQueryWrapper<AreaProvince> provinceQuery = new LambdaQueryWrapper<>();
        provinceQuery.eq(AreaProvince::getAdcode, adcode);
        AreaProvince province = provinceMapper.selectOne(provinceQuery);
        if (province != null) {
            return convertProvinceToResponse(province);
        }
        
        // 再从城市查找
        LambdaQueryWrapper<AreaCity> cityQuery = new LambdaQueryWrapper<>();
        cityQuery.eq(AreaCity::getAdcode, adcode);
        AreaCity city = cityMapper.selectOne(cityQuery);
        if (city != null) {
            return convertCityToResponse(city);
        }
        
        // 最后从区县查找
        LambdaQueryWrapper<AreaDistrict> districtQuery = new LambdaQueryWrapper<>();
        districtQuery.eq(AreaDistrict::getAdcode, adcode);
        AreaDistrict district = districtMapper.selectOne(districtQuery);
        if (district != null) {
            return convertDistrictToResponse(district);
        }
        
        return null;
    }

    /**
     * 转换省份实体为响应DTO
     */
    private AreaDivisionResponse convertProvinceToResponse(AreaProvince province) {
        AreaDivisionResponse response = new AreaDivisionResponse();
        BeanUtils.copyProperties(province, response);
        response.setLevel("province");
        response.setParentCode(province.getCountryAdcode());
        return response;
    }

    /**
     * 转换城市实体为响应DTO
     */
    private AreaDivisionResponse convertCityToResponse(AreaCity city) {
        AreaDivisionResponse response = new AreaDivisionResponse();
        BeanUtils.copyProperties(city, response);
        response.setLevel("city");
        response.setParentCode(city.getProvinceAdcode());
        response.setCitycode(city.getCitycode());
        return response;
    }

    /**
     * 转换区县实体为响应DTO
     */
    private AreaDivisionResponse convertDistrictToResponse(AreaDistrict district) {
        AreaDivisionResponse response = new AreaDivisionResponse();
        BeanUtils.copyProperties(district, response);
        response.setLevel("district");
        response.setParentCode(district.getCityAdcode());
        return response;
    }

    @Override
    @Cacheable(value = "area:streets", key = "#districtCode")
    public List<StreetResponse> getStreetsByDistrict(String districtCode) {
        log.info("根据区县代码获取街道列表: {}", districtCode);
        LambdaQueryWrapper<AreaStreet> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaStreet::getDistrictAdcode, districtCode)
                   .orderBy(true, true, AreaStreet::getName);
        List<AreaStreet> streets = streetMapper.selectList(queryWrapper);
        return streets.stream().map(this::convertStreetToResponse).collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "area:districts-page", key = "#current + ':' + #pageSize + ':' + (#keyword != null ? #keyword : 'null') + ':' + (#adcode != null ? #adcode : 'null')")
    public IPage<AreaDivisionResponse> getDistrictsPage(Integer current, Integer pageSize, String keyword, String adcode) {
        log.info("分页查询区县列表: current={}, pageSize={}, keyword={}, adcode={}", current, pageSize, keyword, adcode);
        
        LambdaQueryWrapper<AreaDistrict> queryWrapper = new LambdaQueryWrapper<>();
        
        // 添加搜索条件
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(AreaDistrict::getName, keyword);
        }
        if (StringUtils.hasText(adcode)) {
            queryWrapper.like(AreaDistrict::getAdcode, adcode);
        }
        
        // 排序
        queryWrapper.orderBy(true, true, AreaDistrict::getName);
        
        // 分页查询
        Page<AreaDistrict> page = new Page<>(current, pageSize);
        Page<AreaDistrict> districtPage = districtMapper.selectPage(page, queryWrapper);
        
        // 转换为响应DTO
        return districtPage.convert(district -> {
            AreaDivisionResponse response = new AreaDivisionResponse();
            BeanUtils.copyProperties(district, response);
            response.setLevel("district");
            
            // 明确设置ID字段，确保不会丢失
            response.setId(district.getId());
            
            // 调试日志：检查ID字段是否被正确复制
            log.debug("区县ID转换调试: district.id={}, response.id={}, district.adcode={}", 
                     district.getId(), response.getId(), district.getAdcode());
            
            // 查询城市和省份名称
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
            
            return response;
        });
    }

    @Override
    @Cacheable(value = "area:streets-page", key = "#current + ':' + #pageSize + ':' + (#keyword != null ? #keyword : 'null') + ':' + (#adcode != null ? #adcode : 'null')")
    public IPage<StreetResponse> getStreetsPage(Integer current, Integer pageSize, String keyword, String adcode) {
        log.info("分页查询街道列表: current={}, pageSize={}, keyword={}, adcode={}", current, pageSize, keyword, adcode);
        
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

    /**
     * 转换街道实体为响应DTO
     */
    private StreetResponse convertStreetToResponse(AreaStreet street) {
        StreetResponse response = new StreetResponse();
        BeanUtils.copyProperties(street, response);
        return response;
    }

    @Override
    public DistrictResponse getDistrictById(Integer id) {
        log.info("根据ID获取区县详情: {}", id);
        AreaDistrict district = districtMapper.selectById(id);
        if (district == null) {
            return null;
        }
        return convertDistrictToDetailResponse(district);
    }

    @Override
    public DistrictResponse getDistrictByAdcode(String adcode) {
        log.info("根据区域代码获取区县详情: {}", adcode);
        LambdaQueryWrapper<AreaDistrict> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AreaDistrict::getAdcode, adcode);
        AreaDistrict district = districtMapper.selectOne(queryWrapper);
        if (district == null) {
            return null;
        }
        return convertDistrictToDetailResponse(district);
    }

    @Override
    public DistrictResponse createDistrict(DistrictCreateRequest request) {
        log.info("创建区县: {}", request);
        AreaDistrict district = new AreaDistrict();
        BeanUtils.copyProperties(request, district);
        districtMapper.insert(district);
        return convertDistrictToDetailResponse(district);
    }

    @Override
    public DistrictResponse updateDistrict(Integer id, DistrictUpdateRequest request) {
        log.info("更新区县: id={}, request={}", id, request);
        AreaDistrict district = districtMapper.selectById(id);
        if (district == null) {
            return null;
        }
        BeanUtils.copyProperties(request, district);
        districtMapper.updateById(district);
        return convertDistrictToDetailResponse(district);
    }

    @Override
    public void deleteDistrict(Integer id) {
        log.info("删除区县: {}", id);
        districtMapper.deleteById(id);
    }

    @Override
    public void deleteDistricts(List<Integer> ids) {
        log.info("批量删除区县: {}", ids);
        districtMapper.deleteBatchIds(ids);
    }

    /**
     * 转换区县实体为详细响应DTO
     */
    private DistrictResponse convertDistrictToDetailResponse(AreaDistrict district) {
        DistrictResponse response = new DistrictResponse();
        BeanUtils.copyProperties(district, response);
        return response;
    }
}
