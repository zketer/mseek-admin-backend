package com.lynn.museum.info.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lynn.museum.info.dto.AmapGeocodeResponse;
import com.lynn.museum.info.dto.CategoryResponse;
import com.lynn.museum.info.dto.MuseumQueryRequest;
import com.lynn.museum.info.dto.MuseumResponse;
import com.lynn.museum.info.dto.NearbyMuseumsResponse;
import com.lynn.museum.info.mapper.AreaCityMapper;
import com.lynn.museum.info.mapper.AreaProvinceMapper;
import com.lynn.museum.info.mapper.MuseumCategoryMapper;
import com.lynn.museum.info.mapper.MuseumInfoMapper;
import com.lynn.museum.info.model.entity.AreaCity;
import com.lynn.museum.info.model.entity.AreaProvince;
import com.lynn.museum.info.model.entity.MuseumCategory;
import com.lynn.museum.info.model.entity.MuseumInfo;
import com.lynn.museum.info.service.AmapGeocodeService;
import com.lynn.museum.info.service.MiniappMuseumService;
import com.lynn.museum.info.service.MuseumInfoService;
import com.lynn.museum.common.redis.utils.RedisUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 小程序博物馆服务实现类
 *
 * @author lynn
 * @since 2024-01-01
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class MiniappMuseumServiceImpl implements MiniappMuseumService {

    private final MuseumInfoMapper museumInfoMapper;
    private final MuseumInfoService museumInfoService;
    private final MuseumCategoryMapper museumCategoryMapper;
    private final AreaCityMapper areaCityMapper;
    private final AreaProvinceMapper areaProvinceMapper;
    private final AmapGeocodeService amapGeocodeService;
    // Redis工具类
    private final RedisUtils redisUtils;
    
    // Redis缓存键前缀
    private static final String GEOCODE_CACHE_PREFIX = "geocode:";
    // 缓存过期时间：24小时（86400秒）
    private static final long GEOCODE_CACHE_EXPIRE = 86400L;

    @Override
    public IPage<MuseumResponse> getMuseumPage(Integer page, Integer pageSize, String cityCode, String keyword, Integer categoryId, String sortBy) {
        LambdaQueryWrapper<MuseumInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索 - 使用三级优先级搜索逻辑
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.trim();
            
            // 第一、二优先级：省份 → 城市 → 地区代码匹配
            List<String> regionCodes = getRegionCodesByName(searchKeyword);
            
            if (!regionCodes.isEmpty()) {
                // 通过地区代码精确匹配
                queryWrapper.and(wrapper -> {
                    for (int i = 0; i < regionCodes.size(); i++) {
                        String code = regionCodes.get(i);
                        if (i == 0) {
                            wrapper.likeRight(MuseumInfo::getProvinceCode, code)
                                   .or().likeRight(MuseumInfo::getCityCode, code);
                        } else {
                            wrapper.or().likeRight(MuseumInfo::getProvinceCode, code)
                                   .or().likeRight(MuseumInfo::getCityCode, code);
                        }
                    }
                });
            } else {
                // 第三优先级：博物馆名称/描述/地址模糊搜索
                log.info("🔍 第三步：使用博物馆内容模糊搜索 '{}'", searchKeyword);
                queryWrapper.and(wrapper -> 
                    wrapper.like(MuseumInfo::getName, searchKeyword)
                           .or()
                           .like(MuseumInfo::getDescription, searchKeyword)
                           .or()
                           .like(MuseumInfo::getAddress, searchKeyword)
                );
            }
        }
        
        // 城市筛选（可选）- 优先级高于关键词搜索
        if (cityCode != null && !cityCode.trim().isEmpty()) {
            queryWrapper.eq(MuseumInfo::getCityCode, cityCode);
        }
        
        // 分类筛选（可选）
        if (categoryId != null && categoryId > 0) {
            // TODO: 实现分类筛选逻辑，需要关联查询分类表
        }
        
        // 只查询启用状态的博物馆
        queryWrapper.eq(MuseumInfo::getStatus, 1);
        
        // 只查询展示状态的博物馆
        queryWrapper.eq(MuseumInfo::getDisplay, 1);
        
        // 排序逻辑
        if ("relevance".equals(sortBy)) {
            // TODO: 实现相关性排序逻辑
            queryWrapper.orderByDesc(MuseumInfo::getUpdateAt);
        } else {
            applySorting(queryWrapper, sortBy);
        }
        
        Page<MuseumInfo> pageRequest = new Page<>(page, pageSize);
        IPage<MuseumInfo> result = museumInfoMapper.selectPage(pageRequest, queryWrapper);
        return convertToResponsePage(result);
    }

    @Override
    public IPage<MuseumResponse> getMuseumsByCity(String cityCode, Integer page, Integer pageSize, String keyword, String sortBy) {
        LambdaQueryWrapper<MuseumInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 城市筛选（必须）
        queryWrapper.eq(MuseumInfo::getCityCode, cityCode);
        
        // 关键词搜索（可选）
        if (keyword != null && !keyword.trim().isEmpty()) {
            queryWrapper.and(wrapper -> 
                wrapper.like(MuseumInfo::getName, keyword)
                       .or()
                       .like(MuseumInfo::getDescription, keyword)
                       .or()
                       .like(MuseumInfo::getAddress, keyword)
            );
        }
        
        // 只查询启用状态的博物馆
        queryWrapper.eq(MuseumInfo::getStatus, 1);
        
        // 只查询展示状态的博物馆
        queryWrapper.eq(MuseumInfo::getDisplay, 1);
        
        // 排序
        applySorting(queryWrapper, sortBy);
        
        Page<MuseumInfo> pageRequest = new Page<>(page, pageSize);
        IPage<MuseumInfo> result = museumInfoMapper.selectPage(pageRequest, queryWrapper);
        
        return convertToResponsePage(result);
    }

    @Override
    public IPage<MuseumResponse> searchMuseums(String keyword, Integer page, Integer pageSize, String cityCode, String sortBy) {
        LambdaQueryWrapper<MuseumInfo> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（必须）- 使用三级优先级搜索
        if (keyword != null && !keyword.trim().isEmpty()) {
            String searchKeyword = keyword.trim();
            
            // 第一、二优先级：省份 → 城市 → 地区代码匹配
            List<String> regionCodes = getRegionCodesByName(searchKeyword);
            
            if (!regionCodes.isEmpty()) {
                // 通过地区代码精确匹配
                queryWrapper.and(wrapper -> {
                    for (int i = 0; i < regionCodes.size(); i++) {
                        String code = regionCodes.get(i);
                        if (i == 0) {
                            wrapper.likeRight(MuseumInfo::getProvinceCode, code)
                                   .or().likeRight(MuseumInfo::getCityCode, code);
                        } else {
                            wrapper.or().likeRight(MuseumInfo::getProvinceCode, code)
                                   .or().likeRight(MuseumInfo::getCityCode, code);
                        }
                    }
                });
            } else {
                // 第三优先级：博物馆名称/描述/地址模糊搜索
                queryWrapper.and(wrapper -> 
                    wrapper.like(MuseumInfo::getName, searchKeyword)
                           .or()
                           .like(MuseumInfo::getDescription, searchKeyword)
                           .or()
                           .like(MuseumInfo::getAddress, searchKeyword)
                );
            }
        }
        
        // 城市筛选（可选）- 优先级高于关键词搜索
        if (cityCode != null && !cityCode.trim().isEmpty()) {
            queryWrapper.eq(MuseumInfo::getCityCode, cityCode);
        }
        
        // 只查询启用状态的博物馆
        queryWrapper.eq(MuseumInfo::getStatus, 1);
        
        // 只查询展示状态的博物馆
        queryWrapper.eq(MuseumInfo::getDisplay, 1);
        
        // 搜索排序（相关性优先）
        if ("relevance".equals(sortBy)) {
            // TODO: 实现相关性排序逻辑
            queryWrapper.orderByDesc(MuseumInfo::getUpdateAt);
        } else {
            applySorting(queryWrapper, sortBy);
        }
        
        Page<MuseumInfo> pageRequest = new Page<>(page, pageSize);
        IPage<MuseumInfo> result = museumInfoMapper.selectPage(pageRequest, queryWrapper);
        
        log.info("✅ 博物馆搜索完成，共找到 {} 个博物馆", result.getTotal());
        return convertToResponsePage(result);
    }

    @Override
    public MuseumResponse getMuseumDetail(Long id) {
        log.info("获取博物馆详情 - ID：{}", id);
        
        // 复用现有的服务方法
        return museumInfoService.getMuseumById(id);
    }

    @Override
    public List<MuseumInfo> getNearbyMuseums(Double latitude, Double longitude, Integer radius, Integer limit) {
        log.info("获取附近博物馆 - 位置：{},{}, 半径：{}km, 限制：{}", latitude, longitude, radius, limit);
        
        // 复用现有的服务方法
        return museumInfoService.getNearbyMuseums(latitude, longitude, radius, limit);
    }


    /**
     * 应用排序条件
     */
    private void applySorting(LambdaQueryWrapper<MuseumInfo> queryWrapper, String sortBy) {
        if (sortBy == null) {
            sortBy = "default";
        }
        
        switch (sortBy) {
            case "rating":
                // TODO: 添加评分字段排序
                queryWrapper.orderByDesc(MuseumInfo::getUpdateAt);
                break;
            case "distance":
                // 距离排序需要在调用处处理，这里使用默认排序
                queryWrapper.orderByDesc(MuseumInfo::getUpdateAt);
                break;
            case "createAt":
                queryWrapper.orderByDesc(MuseumInfo::getCreateAt);
                break;
            default:
                queryWrapper.orderByDesc(MuseumInfo::getUpdateAt);
                break;
        }
    }

    /**
     * 转换为响应分页对象
     */
    private IPage<MuseumResponse> convertToResponsePage(IPage<MuseumInfo> page) {
        Page<MuseumResponse> responsePage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        
        // 先转换基础数据，不填充图片
        List<MuseumResponse> responseList = page.getRecords().stream()
                .map(this::convertToResponseWithoutImages)
                .collect(Collectors.toList());
        
        // 批量填充图片URL，避免N+1查询
        log.info("🖼️ 开始批量填充图片URL，博物馆数量：{}", responseList.size());
        ((MuseumInfoServiceImpl) museumInfoService).batchFillImageUrls(responseList);
        log.info("🖼️ 批量填充图片URL完成");
        
        responsePage.setRecords(responseList);
        return responsePage;
    }

    /**
     * 转换为响应对象（不填充图片，用于批量场景）
     */
    private MuseumResponse convertToResponseWithoutImages(MuseumInfo museumInfo) {
        MuseumResponse response = new MuseumResponse();
        BeanUtils.copyProperties(museumInfo, response);
        return response;
    }

    /**
     * 转换为响应对象（填充图片，用于单个博物馆场景）
     */
    private MuseumResponse convertToResponse(MuseumInfo museumInfo) {
        MuseumResponse response = new MuseumResponse();
        BeanUtils.copyProperties(museumInfo, response);
        
        // 填充图片URL信息
        ((MuseumInfoServiceImpl) museumInfoService).fillImageUrls(response);
        
        return response;
    }

    @Override
    public IPage<MuseumResponse> getHotMuseums(Integer page, Integer pageSize, String name) {
        log.info("分页获取热门博物馆列表 - 页码：{}，页面大小：{}，名称：{}", page, pageSize, name);

        Page<MuseumResponse> pageRequest = new Page<>(page, pageSize);
        IPage<MuseumResponse> result = museumInfoMapper.selectHotMuseums(pageRequest, name);
        
        // 批量填充图片URL，避免N+1查询
        if (result.getRecords() != null && !result.getRecords().isEmpty()) {
            log.info("🔥 开始为热门博物馆批量填充图片URL，数量：{}", result.getRecords().size());
            ((MuseumInfoServiceImpl) museumInfoService).batchFillImageUrls(result.getRecords());
            log.info("🔥 热门博物馆图片URL填充完成");
        }
        
        return result;
    }

    @Override
    public List<CategoryResponse> getCategories() {
        log.info("获取博物馆分类列表");
        
        // 查询所有启用状态的分类，按排序字段排序
        LambdaQueryWrapper<MuseumCategory> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(MuseumCategory::getStatus, 1)
                   .orderByAsc(MuseumCategory::getSortOrder)
                   .orderByAsc(MuseumCategory::getId);
        
        List<MuseumCategory> categories = museumCategoryMapper.selectList(queryWrapper);
        
        // 转换为响应DTO
        return categories.stream()
                .map(this::convertToCategoryResponse)
                .collect(Collectors.toList());
    }

    @Override
    public IPage<MuseumResponse> getNearbyMuseumsPage(Double latitude, Double longitude, Integer radius, Integer page, Integer pageSize) {
        log.info("分页获取附近博物馆 - 位置：{},{}, 半径：{}km, 页码：{}, 大小：{}", 
                latitude, longitude, radius, page, pageSize);

        // 1. 调用高德地图API获取城市信息
        String amapCityCode = getCityCodeFromAmap(latitude, longitude);
        if (amapCityCode == null) {
            log.warn("无法获取城市编码，返回空结果");
            // 返回空的分页结果
            return new Page<>(page, pageSize);
        }
        
        String cityCode = mapAmapCityCodeToDatabase(amapCityCode);
        if (cityCode == null) {
            log.warn("城市编码映射失败，返回空结果");
            // 返回空的分页结果
            return new Page<>(page, pageSize);
        }
        
        log.info("通过高德地图API获取到城市编码：{} -> 数据库编码：{}", amapCityCode, cityCode);

        // 2. 构建查询请求，基于城市编码查询
        MuseumQueryRequest query = new MuseumQueryRequest();
        query.setPage(page);
        query.setSize(pageSize);
        // 只查询启用状态的博物馆
        query.setStatus(1);
        // 设置城市编码进行筛选
        query.setCityCode(cityCode);
        
        // 3. 分页查询该城市的博物馆
        Page<MuseumInfo> museumInfoPage = new Page<>(page, pageSize);
        IPage<MuseumResponse> result = museumInfoMapper.selectMuseumPage(museumInfoPage, query);
        
        // 4. 计算每个博物馆与用户位置的距离，并按距离排序
        List<MuseumResponse> museums = result.getRecords();
        museums.forEach(museum -> {
            if (museum.getLatitude() != null && museum.getLongitude() != null) {
                double distance = calculateDistance(latitude, longitude, 
                    museum.getLatitude().doubleValue(), museum.getLongitude().doubleValue());
                
                // 这里可以设置距离字段到museum对象中，需要确保MuseumResponse有distance字段
                log.debug("博物馆 {} 距离用户 {} 公里", museum.getName(), distance);
            }
        });
        
        // 5. 按距离排序（距离近的优先）
        museums.sort((m1, m2) -> {
            if (m1.getLatitude() == null || m1.getLongitude() == null) {
                return 1;
            }
            if (m2.getLatitude() == null || m2.getLongitude() == null) {
                return -1;
            }
            
            double distance1 = calculateDistance(latitude, longitude, 
                m1.getLatitude().doubleValue(), m1.getLongitude().doubleValue());
            double distance2 = calculateDistance(latitude, longitude, 
                m2.getLatitude().doubleValue(), m2.getLongitude().doubleValue());
            
            return Double.compare(distance1, distance2);
        });
        
        // 重新设置排序后的数据
        result.setRecords(museums);
        
        log.info("附近博物馆查询完成 - 城市编码：{}, 返回博物馆数量：{}", cityCode, museums.size());
        return result;
    }

    /**
     * 通过高德地图API获取城市编码
     */
    private String getCityCodeFromAmap(Double latitude, Double longitude) {
        try {
            // 调用高德地图逆地理编码API
            AmapGeocodeResponse response = amapGeocodeService.reverseGeocode(longitude, latitude);
            
            if (response != null && response.getRegeocode() != null) {
                AmapGeocodeResponse.Regeocode regeocode = response.getRegeocode();
                if (regeocode.getAddressComponent() != null) {
                    String cityCode = regeocode.getAddressComponent().getCitycode();
                    String adCode = regeocode.getAddressComponent().getAdcode();
                    
                    log.info("高德地图API返回 - 城市代码：{}, 区域代码：{}, 城市：{}", 
                        cityCode, adCode, regeocode.getAddressComponent().getCity());
                    
                    // 优先返回citycode，通过area_cities表进行映射
                    if (cityCode != null && !cityCode.isEmpty()) {
                        log.info("获取到高德地图citycode：{}", cityCode);
                        return cityCode;
                    }
                    
                    // 备用：如果citycode不可用，尝试从adcode推断citycode
                    if (adCode != null && !adCode.isEmpty()) {
                        log.warn("citycode不可用，尝试使用adcode：{}", adCode);
                        return adCode;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("调用高德地图API获取城市编码失败 - 位置：{},{}", latitude, longitude, e);
        }
        
        // 如果高德API调用失败，返回null，让调用方处理
        log.warn("高德地图API调用失败，无法获取城市编码");
        return null;
    }

    /**
     * 计算两点间距离（简单的球面距离计算）
     */
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        // 地球半径（公里）
        final int R = 6371;
        
        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c;
        
        // 保留两位小数
        return Math.round(distance * 100.0) / 100.0;
    }

    @Override
    public NearbyMuseumsResponse getNearbyMuseumsWithLocation(Double latitude, Double longitude, Integer radius, Integer page, Integer pageSize, String name, String cityCode, String cityName) {
        log.info("获取附近博物馆（包含位置信息）- 位置：{},{}, 半径：{}km, 页码：{}, 大小：{}, 搜索：{}, 前端传入城市：{}({})",
                latitude, longitude, radius, page, pageSize, name, cityName, cityCode);

        // 1. ✅ 附近博物馆必须基于当前经纬度获取城市信息，不使用前端缓存的cityCode
        //    原因：用户可能跨城市移动，必须根据实时位置查询
        //    性能：会优先使用Redis缓存（基于经纬度的缓存key）
        log.info("📍 附近博物馆查询：忽略前端传入的cityCode，根据当前经纬度重新获取城市信息（会使用Redis缓存）");
        NearbyMuseumsResponse.LocationInfo locationInfo = getLocationInfoFromAmap(latitude, longitude);

        // 2. 使用位置信息中的城市编码获取附近博物馆列表（支持搜索和半径过滤）
        String useCityCode = locationInfo.getCityCode();
        IPage<MuseumResponse> museumPage = getNearbyMuseumsPageByCityCode(latitude, longitude, useCityCode, radius, page, pageSize, name);

        // 3. 构建完整响应
        return NearbyMuseumsResponse.builder()
                .location(locationInfo)
                .museums(museumPage)
                .build();
    }

    /**
     * 通过高德地图API获取详细位置信息（支持Redis缓存）
     * 
     * 缓存策略：
     * - Key: geocode:lat:lng（保留4位小数）
     * - 有效期: 24小时
     * - 相同位置（精度50米）共享缓存，节省API调用
     */
    private NearbyMuseumsResponse.LocationInfo getLocationInfoFromAmap(Double latitude, Double longitude) {
        // 1. 生成Redis缓存键（保留4位小数，约等于11米精度）
        String cacheKey = generateGeocodeKey(latitude, longitude);
        
        // 2. 尝试从Redis获取缓存
        try {
            Object cached = redisUtils.get(cacheKey);
            if (cached instanceof NearbyMuseumsResponse.LocationInfo) {
                log.info("✅ Redis缓存命中 - Key: {}, 城市: {}", 
                        cacheKey, ((NearbyMuseumsResponse.LocationInfo) cached).getCityName());
                return (NearbyMuseumsResponse.LocationInfo) cached;
            }
        } catch (Exception e) {
            log.warn("⚠️ Redis缓存读取失败，继续调用高德API - {}", e.getMessage());
        }
        
        // 3. 缓存未命中，调用高德地图API
        log.info("❌ Redis缓存未命中 - Key: {}, 开始调用高德地图API", cacheKey);
        
        NearbyMuseumsResponse.LocationInfo.LocationInfoBuilder builder = NearbyMuseumsResponse.LocationInfo.builder()
                .latitude(latitude)
                .longitude(longitude);

        try {
            // 调用高德地图逆地理编码API
            AmapGeocodeResponse response = amapGeocodeService.reverseGeocode(longitude, latitude);

            if (response != null && response.getRegeocode() != null) {
                AmapGeocodeResponse.Regeocode regeocode = response.getRegeocode();
                if (regeocode.getAddressComponent() != null) {
                    AmapGeocodeResponse.AddressComponent addr = regeocode.getAddressComponent();
                    
                    builder.cityName(addr.getCity())
                           .cityCode(addr.getCitycode())
                           .province(addr.getProvince())
                           .district(addr.getDistrict())
                           .formattedAddress(regeocode.getFormattedAddress());
                    
                    log.info("✅ 高德API调用成功 - 城市：{}, 地址：{}", addr.getCity(), regeocode.getFormattedAddress());
                }
            }
        } catch (Exception e) {
            log.warn("❌ 调用高德地图API失败 - 位置：{},{}, 错误：{}", latitude, longitude, e.getMessage());
            // 设置默认值
            builder.cityName("未知城市")
                   .cityCode("000000")
                   .province("未知省份")
                   .district("未知区域")
                   .formattedAddress("位置获取失败");
        }

        NearbyMuseumsResponse.LocationInfo locationInfo = builder.build();
        
        // 4. 将结果存入Redis缓存（24小时）
        try {
            redisUtils.set(cacheKey, locationInfo, GEOCODE_CACHE_EXPIRE);
            log.info("💾 位置信息已缓存到Redis - Key: {}, 过期时间: {}小时", cacheKey, GEOCODE_CACHE_EXPIRE / 3600);
        } catch (Exception e) {
            log.warn("⚠️ Redis缓存写入失败 - {}", e.getMessage());
        }

        return locationInfo;
    }
    
    /**
     * 生成逆地理编码缓存键
     * 
     * 经纬度保留4位小数（约11米精度），相同位置共享缓存
     * 示例: geocode:38.8703:121.5616
     */
    private String generateGeocodeKey(Double latitude, Double longitude) {
        BigDecimal lat = BigDecimal.valueOf(latitude).setScale(4, RoundingMode.HALF_UP);
        BigDecimal lng = BigDecimal.valueOf(longitude).setScale(4, RoundingMode.HALF_UP);
        return GEOCODE_CACHE_PREFIX + lat + ":" + lng;
    }

    /**
     * 智能获取位置信息（优先使用前端传递的信息，避免重复调用高德API）
     */
    private NearbyMuseumsResponse.LocationInfo getLocationInfoSmart(Double latitude, Double longitude, String cityCode, String cityName) {
        // 如果前端已经传递了城市信息，优先使用
        if (cityCode != null && !cityCode.trim().isEmpty() && cityName != null && !cityName.trim().isEmpty()) {
            log.info("使用前端传递的城市信息 - 城市：{}({})", cityName, cityCode);
            return NearbyMuseumsResponse.LocationInfo.builder()
                    .latitude(latitude)
                    .longitude(longitude)
                    .cityName(cityName)
                    .cityCode(cityCode)
                    // 简化地址信息
                    .formattedAddress(cityName)
                    .build();
        }

        // 如果前端没有传递完整城市信息，才调用高德API
        log.info("前端未提供完整城市信息，调用高德地图API获取");
        return getLocationInfoFromAmap(latitude, longitude);
    }

    /**
     * 基于城市编码和半径获取附近博物馆（支持距离过滤和搜索）
     */
    private IPage<MuseumResponse> getNearbyMuseumsPageByCityCode(Double latitude, Double longitude, String cityCode, Integer radius, Integer page, Integer pageSize, String name) {
        log.info("📍 基于城市编码和半径获取博物馆 - 城市：{}, 半径：{}km, 页码：{}, 大小：{}, 搜索：{}", cityCode, radius, page, pageSize, name);

        // 1. 映射高德地图城市编码到数据库城市编码
        String databaseCityCode = mapAmapCityCodeToDatabase(cityCode);
        log.debug("城市编码映射：高德编码[{}] -> 数据库编码[{}]", cityCode, databaseCityCode);

        // 2. 构建查询请求（查询所有符合条件的博物馆，不预先分页）
        MuseumQueryRequest query = new MuseumQueryRequest();
        // 只查询启用状态的博物馆
        query.setStatus(1);
        // 城市编码
        query.setCityCode(databaseCityCode);
        // 搜索关键词
        query.setName(name);
        
        // 查询该城市所有符合条件的博物馆
        // 使用足够大的页面大小
        Page<MuseumInfo> allMuseumsPage = new Page<>(1, 10000);
        IPage<MuseumResponse> allMuseumsResult = museumInfoMapper.selectMuseumPage(allMuseumsPage, query);
        List<MuseumResponse> allMuseums = allMuseumsResult.getRecords();
        
        log.debug("从数据库查询到 {} 家博物馆（过滤前）", allMuseums.size());

        // 3. 计算距离、过滤、排序
        List<MuseumResponse> nearbyMuseums = allMuseums.stream()
            .peek(museum -> {
                // 计算距离并设置到museum对象
                if (museum.getLatitude() != null && museum.getLongitude() != null) {
                    double distanceKm = calculateDistance(
                        latitude, longitude,
                        museum.getLatitude().doubleValue(), 
                        museum.getLongitude().doubleValue()
                    );
                    museum.setDistance(formatDistance(distanceKm));
                } else {
                    museum.setDistance("未知");
                }
            })
            .filter(museum -> {
                // ✅ 根据radius过滤距离
                if (radius != null && museum.getDistance() != null && !"未知".equals(museum.getDistance())) {
                    double distanceKm = parseDistance(museum.getDistance());
                    boolean inRange = distanceKm <= radius;
                    if (!inRange) {
                        log.debug("过滤掉：{} (距离：{}，超出{}km范围)", museum.getName(), museum.getDistance(), radius);
                    }
                    return inRange;
                }
                // 如果没有指定radius，则保留所有博物馆
                return true;
            })
            .sorted((m1, m2) -> {
                // 按距离排序（距离近的优先）
                double d1 = parseDistance(m1.getDistance());
                double d2 = parseDistance(m2.getDistance());
                return Double.compare(d1, d2);
            })
            .collect(Collectors.toList());
        
        log.info("✅ 过滤后：半径{}km内有 {} 家博物馆", radius, nearbyMuseums.size());

        // 4. 手动分页
        int total = nearbyMuseums.size();
        int start = (page - 1) * pageSize;
        int end = Math.min(start + pageSize, total);
        
        List<MuseumResponse> pageData;
        if (start < total) {
            pageData = nearbyMuseums.subList(start, end);
            log.debug("分页结果：第{}页，从索引{}到{}，返回{}条", page, start, end - 1, pageData.size());
        } else {
            pageData = new ArrayList<>();
            log.debug("分页结果：第{}页超出范围，返回空列表", page);
        }

        // 5. 批量填充图片URL，避免N+1查询
        if (!pageData.isEmpty()) {
            log.info("🏛️ 开始为附近博物馆批量填充图片URL，数量：{}", pageData.size());
            ((MuseumInfoServiceImpl) museumInfoService).batchFillImageUrls(pageData);
            log.info("🏛️ 附近博物馆图片URL填充完成");
        }

        // 6. 构建分页结果
        Page<MuseumResponse> result = new Page<>(page, pageSize, total);
        result.setRecords(pageData);
        
        return result;
    }

    /**
     * 格式化距离（保留1位小数，<1km显示为米）
     */
    private String formatDistance(double km) {
        if (km < 1) {
            return Math.round(km * 1000) + "m";
        } else {
            return String.format("%.1f", km) + "km";
        }
    }
    
    /**
     * 解析距离字符串为公里数
     */
    private double parseDistance(String distanceStr) {
        if (distanceStr == null || "未知".equals(distanceStr)) {
            return Double.MAX_VALUE;
        }
        
        try {
            if (distanceStr.endsWith("km")) {
                return Double.parseDouble(distanceStr.replace("km", "").trim());
            } else if (distanceStr.endsWith("m")) {
                double meters = Double.parseDouble(distanceStr.replace("m", "").trim());
                return meters / 1000.0;
            } else {
                return Double.parseDouble(distanceStr);
            }
        } catch (NumberFormatException e) {
            log.warn("⚠️ 解析距离失败: {}", distanceStr);
            return Double.MAX_VALUE;
        }
    }

    /**
     * 动态映射高德地图城市编码到数据库城市编码
     * 通过area_cities表查询adcode
     */
    private String mapAmapCityCodeToDatabase(String amapCityCode) {
        if (amapCityCode == null || amapCityCode.trim().isEmpty()) {
            log.warn("城市编码为空，无法查询博物馆");
            return null;
        }
        
        try {
            // 通过高德citycode查询area_cities表获取adcode
            String adcode = queryCityAdcodeFromDatabase(amapCityCode);
            if (adcode != null) {
                log.info("城市编码映射成功：{} -> {}", amapCityCode, adcode);
                return adcode;
            } else {
                // 如果是adcode格式，直接使用
                if (amapCityCode.length() == 6 && amapCityCode.matches("\\d+")) {
                    log.info("直接使用adcode格式：{}", amapCityCode);
                    return amapCityCode;
                }
                log.warn("无法映射城市编码：{}", amapCityCode);
                return null;
            }
        } catch (Exception e) {
            log.error("查询城市编码映射失败：{}", amapCityCode, e);
            return null;
        }
    }
    
    /**
     * 从数据库查询城市adcode
     */
    private String queryCityAdcodeFromDatabase(String cityCode) {
        try {
            String adcode = areaCityMapper.selectAdcodeByCitycode(cityCode);
            log.info("数据库查询城市编码：{} -> {}", cityCode, adcode);
            return adcode;
        } catch (Exception e) {
            log.error("查询数据库城市编码失败：{}", cityCode, e);
            return null;
        }
    }

    /**
     * 转换为分类响应DTO
     */
    private CategoryResponse convertToCategoryResponse(MuseumCategory category) {
        CategoryResponse response = new CategoryResponse();
        BeanUtils.copyProperties(category, response);
        return response;
    }

    /**
     * 根据地区名称获取地区代码列表
     * 优先级搜索：省份 → 城市
     * @param regionName 地区名称（省份或城市）
     * @return 地区代码列表
     */
    private List<String> getRegionCodesByName(String regionName) {
        List<String> regionCodes = new ArrayList<>();
        
        try {
            // 第一优先级：查找匹配的省份
            log.info("🔍 第一步：在省份中搜索 '{}'", regionName);
            LambdaQueryWrapper<AreaProvince> provinceWrapper = new LambdaQueryWrapper<>();
            provinceWrapper.like(AreaProvince::getName, regionName);
            List<AreaProvince> provinces = areaProvinceMapper.selectList(provinceWrapper);
            
            if (!provinces.isEmpty()) {
                log.info("✅ 在省份中找到匹配：{}", provinces.get(0).getName());
                String provinceCode = provinces.get(0).getAdcode();
                regionCodes.add(provinceCode);
                return regionCodes;
            }
            
            // 第二优先级：查找匹配的城市
            log.info("🔍 第二步：在城市中搜索 '{}'", regionName);
            LambdaQueryWrapper<AreaCity> cityWrapper = new LambdaQueryWrapper<>();
            cityWrapper.like(AreaCity::getName, regionName);
            List<AreaCity> cities = areaCityMapper.selectList(cityWrapper);
            
            if (!cities.isEmpty()) {
                log.info("✅ 在城市中找到匹配：{}", cities.get(0).getName());
                String cityCode = cities.get(0).getAdcode();
                regionCodes.add(cityCode);
                return regionCodes;
            }
            
            // 没有找到地区匹配
            log.info("❌ 未在省份和城市中找到匹配: {}", regionName);
            return regionCodes;
            
        } catch (Exception e) {
            log.error("根据地区名称查找地区代码失败: {}", regionName, e);
            return new ArrayList<>();
        }
    }
}
