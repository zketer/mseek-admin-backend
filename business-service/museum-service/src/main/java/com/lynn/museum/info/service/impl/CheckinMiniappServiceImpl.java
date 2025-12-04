package com.lynn.museum.info.service.impl;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lynn.museum.common.exception.BizException;
import com.lynn.museum.info.dto.*;
import com.lynn.museum.info.mapper.CheckinRecordMapper;
import com.lynn.museum.info.mapper.MuseumInfoMapper;
import com.lynn.museum.info.mapper.AreaProvinceMapper;
import com.lynn.museum.info.mapper.AreaCityMapper;
import com.lynn.museum.info.model.entity.AreaProvince;
import com.lynn.museum.info.model.entity.AreaCity;
import com.lynn.museum.info.model.entity.CheckinRecord;
import com.lynn.museum.info.model.entity.MuseumInfo;
import com.lynn.museum.info.service.CheckinMiniappService;
import com.lynn.museum.info.service.AutoAuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 小程序打卡服务实现
 *
 * @author lynn
 * @since 2024-12-30
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CheckinMiniappServiceImpl implements CheckinMiniappService {

    private final CheckinRecordMapper checkinRecordMapper;
    private final MuseumInfoMapper museumInfoMapper;
    private final AreaProvinceMapper areaProvinceMapper;
    private final AreaCityMapper areaCityMapper;
    private final ObjectMapper objectMapper;
    private final AutoAuditService autoAuditService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinSubmitResponse submitCheckin(CheckinSubmitRequest request, Long userId) {
        try {
            CheckinRecord checkinRecord = buildCheckinRecord(request, userId);
            
            // 如果是更新暂存记录（draftId实际上是记录的主键ID）
            if (StringUtils.hasText(request.getDraftId())) {
                try {
                    Long recordId = Long.parseLong(request.getDraftId());
                    
                    // 验证记录是否存在且属于当前用户
                    CheckinRecord existingRecord = checkinRecordMapper.selectById(recordId);
                    if (existingRecord != null && existingRecord.getUserId().equals(userId)) {
                        log.info("更新已有记录: ID={}, isDraft={}", recordId, request.getIsDraft());
                        
                        checkinRecord.setId(recordId);
                        // 保留创建时间
                        checkinRecord.setCreateAt(existingRecord.getCreateAt());
                        checkinRecord.setUpdateAt(new Date());
                        
                        // 如果从草稿转为正式打卡，需要执行自动审核
                        if (!request.getIsDraft() && existingRecord.getIsDraft()) {
                            // 设置为待审核
                            checkinRecord.setAuditStatus(0);
                            checkinRecordMapper.updateById(checkinRecord);
                            
                            // 执行自动审核
                            AutoAuditResponse auditResult = performAutoAuditForRecord(checkinRecord);
                            updateAuditResult(checkinRecord.getId(), auditResult);
                            
                            log.info("草稿转正式打卡，ID: {}, 审核状态: {}", 
                                    checkinRecord.getId(), auditResult.getAuditStatus());
                        } else {
                            checkinRecordMapper.updateById(checkinRecord);
                        }
                        
                        String message = request.getIsDraft() ? "暂存更新成功" : "打卡成功";
                        return CheckinSubmitResponse.success(checkinRecord.getId(), checkinRecord.getCheckinTime(), message);
                    } else {
                        log.warn("记录不存在或不属于当前用户: recordId={}, userId={}", recordId, userId);
                    }
                } catch (NumberFormatException e) {
                    log.warn("draftId不是有效的数字: {}", request.getDraftId());
                }
            }
            
            // 新建记录
            log.info("创建新记录: museumId={}, isDraft={}", request.getMuseumId(), request.getIsDraft());
            
            // 设置初始审核状态为待审核
            checkinRecord.setAuditStatus(0);
            checkinRecordMapper.insert(checkinRecord);
            
            // 如果是正式打卡，执行自动审核并清理暂存记录
            if (!request.getIsDraft()) {
                // 执行自动审核
                AutoAuditResponse auditResult = performAutoAuditForRecord(checkinRecord);
                
                // 更新审核结果
                updateAuditResult(checkinRecord.getId(), auditResult);
                
                // 清理该博物馆的暂存记录
                cleanupDraftsForMuseum(userId, request.getMuseumId());
                
                log.info("打卡记录创建完成，ID: {}, 审核状态: {}", 
                        checkinRecord.getId(), auditResult.getAuditStatus());
            }
            
            String message = request.getIsDraft() ? "暂存成功" : "打卡成功";
            return CheckinSubmitResponse.success(checkinRecord.getId(), checkinRecord.getCheckinTime(), message);
            
        } catch (Exception e) {
            log.error("提交打卡失败，用户ID: {}, 博物馆ID: {}", userId, request.getMuseumId(), e);
            throw new BizException("提交失败：" + e.getMessage());
        }
    }

    @Override
    public IPage<CheckinRecord> getCheckinRecords(CheckinRecordQueryRequest query) {
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, query.getUserId())
               .eq(query.getMuseumId() != null, CheckinRecord::getMuseumId, query.getMuseumId())
               .eq(query.getIsDraft() != null, CheckinRecord::getIsDraft, query.getIsDraft())
               .eq(CheckinRecord::getDeleted, false)
               .orderByDesc(CheckinRecord::getCheckinTime);
        
        // 添加关键词搜索 - 三级优先级搜索
        if (StringUtils.hasText(query.getKeyword())) {
            String keyword = query.getKeyword().trim();
            
            // 第一、二优先级：省份 → 城市 → 获取博物馆ID
            List<Long> regionMuseumIds = getMuseumIdsByRegionName(keyword);
            
            if (!regionMuseumIds.isEmpty()) {
                // 通过地区代码找到博物馆，使用精确ID匹配
                wrapper.in(CheckinRecord::getMuseumId, regionMuseumIds);
            } else {
                // 第三优先级：博物馆名称模糊搜索
                wrapper.and(w -> w
                    .like(CheckinRecord::getMuseumName, keyword)
                    .or()
                    .like(CheckinRecord::getAddress, keyword));
            }
        }
        
        // 处理筛选类型（时间范围）
        if (StringUtils.hasText(query.getFilterType()) && !"all".equals(query.getFilterType())) {
            LocalDateTime now = LocalDateTime.now();
            
            if ("thisMonth".equals(query.getFilterType())) {
                // 本月：当月1号到月末最后一天
                LocalDateTime monthStart = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime monthEnd = now.withDayOfMonth(now.toLocalDate().lengthOfMonth())
                                           .withHour(23).withMinute(59).withSecond(59);
                wrapper.between(CheckinRecord::getCheckinTime, monthStart, monthEnd);
                
            } else if ("thisYear".equals(query.getFilterType())) {
                // 今年：1月1号到12月31号
                LocalDateTime yearStart = now.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
                LocalDateTime yearEnd = now.withDayOfYear(now.toLocalDate().lengthOfYear())
                                          .withHour(23).withMinute(59).withSecond(59);
                wrapper.between(CheckinRecord::getCheckinTime, 
                    Date.from(yearStart.atZone(ZoneId.systemDefault()).toInstant()), 
                    Date.from(yearEnd.atZone(ZoneId.systemDefault()).toInstant()));
            }
        }
        
        // 添加时间范围查询（优先级高于filterType）
        if (StringUtils.hasText(query.getStartDate())) {
            wrapper.ge(CheckinRecord::getCheckinTime, query.getStartDate());
        }
        if (StringUtils.hasText(query.getEndDate())) {
            wrapper.le(CheckinRecord::getCheckinTime, query.getEndDate());
        }
        
        Page<CheckinRecord> page = new Page<>(query.getPage(), query.getPageSize());
        return checkinRecordMapper.selectPage(page, wrapper);
    }

    @Override
    public CheckinRecord getCheckinDetail(Long checkinId, Long userId) {
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getId, checkinId)
               .eq(CheckinRecord::getUserId, userId);
        
        CheckinRecord record = checkinRecordMapper.selectOne(wrapper);
        if (record == null) {
            throw new BizException("打卡记录不存在");
        }
        
        return record;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean deleteDraft(String draftId, Long userId) {
        // draftId实际上是打卡记录的主键ID
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getId, Long.parseLong(draftId))
               .eq(CheckinRecord::getUserId, userId)
               .eq(CheckinRecord::getIsDraft, true);
        
        int deleted = checkinRecordMapper.delete(wrapper);
        if (deleted > 0) {
            log.info("删除草稿: ID={}, userId={}", draftId, userId);
        }
        return deleted > 0;
    }

    @Override
    public CheckinStatsResponse getCheckinStats(Long userId) {
        CheckinStatsResponse stats = new CheckinStatsResponse();
        
        // 总打卡次数（不包括暂存）
        LambdaQueryWrapper<CheckinRecord> totalWrapper = new LambdaQueryWrapper<>();
        totalWrapper.eq(CheckinRecord::getUserId, userId)
                   .eq(CheckinRecord::getIsDraft, false);
        Long totalCheckins = checkinRecordMapper.selectCount(totalWrapper);
        stats.setTotalCheckins(totalCheckins.intValue());
        
        // 本月打卡次数
        YearMonth currentMonth = YearMonth.now();
        LocalDateTime monthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime monthEnd = currentMonth.atEndOfMonth().atTime(23, 59, 59);
        
        LambdaQueryWrapper<CheckinRecord> monthWrapper = new LambdaQueryWrapper<>();
        monthWrapper.eq(CheckinRecord::getUserId, userId)
                   .eq(CheckinRecord::getIsDraft, false)
                   .ge(CheckinRecord::getCheckinTime, Date.from(monthStart.atZone(ZoneId.systemDefault()).toInstant()))
                   .le(CheckinRecord::getCheckinTime, Date.from(monthEnd.atZone(ZoneId.systemDefault()).toInstant()));
        Long thisMonthCheckins = checkinRecordMapper.selectCount(monthWrapper);
        stats.setThisMonthCheckins(thisMonthCheckins.intValue());
        
        // 已访问博物馆数量（去重）
        List<CheckinRecord> distinctMuseums = checkinRecordMapper.selectDistinctMuseumsByUser(userId);
        stats.setVisitedMuseums(distinctMuseums.size());
        
        // 总照片数量（需要解析JSON）
        List<CheckinRecord> recordsWithPhotos = checkinRecordMapper.selectList(
            new LambdaQueryWrapper<CheckinRecord>()
                .eq(CheckinRecord::getUserId, userId)
                .eq(CheckinRecord::getIsDraft, false)
                .isNotNull(CheckinRecord::getPhotos)
        );
        
        int totalPhotos = recordsWithPhotos.stream()
            .mapToInt(this::countPhotosInRecord)
            .sum();
        stats.setTotalPhotos(totalPhotos);
        
        return stats;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public CheckinSubmitResponse convertDraftToCheckin(String draftId, Long userId) {
        // 查找暂存记录
        CheckinRecord draftRecord = findDraftByDraftId(draftId, userId);
        if (draftRecord == null) {
            throw new BizException("暂存记录不存在");
        }
        
        // 转换为正式打卡
        draftRecord.setIsDraft(false);
        draftRecord.setCheckinTime(new Date());
        checkinRecordMapper.updateById(draftRecord);
        
        // 清理该博物馆的其他暂存记录
        cleanupDraftsForMuseum(userId, draftRecord.getMuseumId());
        
        return CheckinSubmitResponse.success(draftRecord.getId(), draftRecord.getCheckinTime(), "转换成功");
    }

    private CheckinRecord buildCheckinRecord(CheckinSubmitRequest request, Long userId) {
        CheckinRecord record = new CheckinRecord();
        record.setUserId(userId);
        record.setMuseumId(request.getMuseumId());
        record.setMuseumName(request.getMuseumName());
        record.setCheckinTime(new Date());
        record.setIsDraft(request.getIsDraft());
        record.setDraftId(request.getDraftId());
        record.setCreateAt(new Date());
        record.setUpdateAt(new Date());
        
        // 位置信息
        if (request.getLocation() != null) {
            record.setLatitude(request.getLocation().getLatitude());
            record.setLongitude(request.getLocation().getLongitude());
            record.setAddress(request.getLocation().getAddress());
        }
        
        // 其他信息
        record.setFeeling(request.getFeeling());
        record.setRating(request.getRating());
        record.setMood(request.getMood());
        record.setWeather(request.getWeather());
        
        // JSON 字段
        try {
            if (request.getPhotos() != null && !request.getPhotos().isEmpty()) {
                record.setPhotos(objectMapper.writeValueAsString(request.getPhotos()));
            }
            if (request.getCompanions() != null && !request.getCompanions().isEmpty()) {
                record.setCompanions(objectMapper.writeValueAsString(request.getCompanions()));
            }
            if (request.getTags() != null && !request.getTags().isEmpty()) {
                record.setTags(objectMapper.writeValueAsString(request.getTags()));
            }
        } catch (Exception e) {
            log.error("序列化JSON字段失败", e);
            throw new BizException("数据格式错误");
        }
        
        return record;
    }

    private CheckinRecord findDraftByDraftId(String draftId, Long userId) {
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getDraftId, draftId)
               .eq(CheckinRecord::getUserId, userId)
               .eq(CheckinRecord::getIsDraft, true);
        return checkinRecordMapper.selectOne(wrapper);
    }

    private void cleanupDraftsForMuseum(Long userId, Long museumId) {
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId)
               .eq(CheckinRecord::getMuseumId, museumId)
               .eq(CheckinRecord::getIsDraft, true);
        checkinRecordMapper.delete(wrapper);
    }

    /**
     * 为打卡记录执行自动审核
     */
    private AutoAuditResponse performAutoAuditForRecord(CheckinRecord checkinRecord) {
        try {
            // 构建自动审核请求
            AutoAuditRequest auditRequest = new AutoAuditRequest();
            auditRequest.setCheckinId(checkinRecord.getId());
            auditRequest.setUserId(checkinRecord.getUserId());
            auditRequest.setMuseumId(checkinRecord.getMuseumId());
            auditRequest.setCheckinTime(checkinRecord.getCheckinTime());
            auditRequest.setLatitude(checkinRecord.getLatitude());
            auditRequest.setLongitude(checkinRecord.getLongitude());
            auditRequest.setPhotoUrls(checkinRecord.getPhotos());
            auditRequest.setRemark(checkinRecord.getFeeling());
            auditRequest.setMood(checkinRecord.getMood());
            // 小程序暂无设备信息
            auditRequest.setDeviceInfo(null);

            // 调用自动审核服务
            return autoAuditService.performAutoAudit(auditRequest);
            
        } catch (Exception e) {
            log.error("自动审核执行失败，打卡记录ID: {}, 错误: {}", checkinRecord.getId(), e.getMessage());
            // 审核失败时，标记为异常，需要人工审核
            return AutoAuditResponse.anomaly("自动审核系统异常：" + e.getMessage(), "system_error");
        }
    }

    /**
     * 更新审核结果
     */
    private void updateAuditResult(Long checkinId, AutoAuditResponse auditResult) {
        LambdaUpdateWrapper<CheckinRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CheckinRecord::getId, checkinId)
                .set(CheckinRecord::getAuditStatus, auditResult.getAuditStatus())
                .set(CheckinRecord::getAuditTime, new Date())
                .set(CheckinRecord::getAuditRemark, auditResult.getAuditRemark())
                .set(CheckinRecord::getAnomalyType, auditResult.getAnomalyType())
                // -1 表示系统自动审核
                .set(CheckinRecord::getAuditUserId, -1L);

        checkinRecordMapper.update(null, updateWrapper);
    }

    private int countPhotosInRecord(CheckinRecord record) {
        try {
            if (StringUtils.hasText(record.getPhotos())) {
                List<String> photos = objectMapper.readValue(record.getPhotos(), new TypeReference<List<String>>() {});
                return photos.size();
            }
        } catch (Exception e) {
            log.warn("解析照片JSON失败，记录ID: {}", record.getId(), e);
        }
        return 0;
    }

    @Override
    public Boolean deleteCheckinRecord(Long checkinId, Long userId) {
        // 查询打卡记录，确保存在且属于当前用户
        LambdaQueryWrapper<CheckinRecord> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(CheckinRecord::getId, checkinId)
                   .eq(CheckinRecord::getUserId, userId)
                   .eq(CheckinRecord::getDeleted, false)
                   // 只能删除正式打卡记录
                   .eq(CheckinRecord::getIsDraft, false);
        
        CheckinRecord existingRecord = checkinRecordMapper.selectOne(queryWrapper);
        if (existingRecord == null) {
            log.warn("用户{}尝试删除不存在或无权限的打卡记录: {}", userId, checkinId);
            throw new RuntimeException("打卡记录不存在或无删除权限");
        }
        
        // 执行软删除
        LambdaUpdateWrapper<CheckinRecord> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(CheckinRecord::getId, checkinId)
                    .eq(CheckinRecord::getUserId, userId)
                    .set(CheckinRecord::getDeleted, true)
                    .set(CheckinRecord::getUpdateAt, new Date());
        
        int updateCount = checkinRecordMapper.update(null, updateWrapper);
        boolean success = updateCount > 0;
        
        if (success) {
            log.info("删除打卡记录: id={}, userId={}", checkinId, userId);
        }
        return success;
    }

    @Override
    public ProvinceCheckinStatsResponse getProvinceStats(Long userId) {
        ProvinceCheckinStatsResponse response = new ProvinceCheckinStatsResponse();
        
        // 获取用户所有正式打卡记录，按省份分组统计
        LambdaQueryWrapper<CheckinRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CheckinRecord::getUserId, userId)
               .eq(CheckinRecord::getIsDraft, false)
               .eq(CheckinRecord::getDeleted, false)
               .orderByDesc(CheckinRecord::getCheckinTime);
        
        List<CheckinRecord> allRecords = checkinRecordMapper.selectList(wrapper);
        
        // 按省份分组统计用户打卡记录
        Map<String, List<CheckinRecord>> userProvinceGroups = allRecords.stream()
                .collect(Collectors.groupingBy(this::extractProvinceName));
        
        // 获取完整的省份列表并统计每个省份的博物馆总数
        List<ProvinceCheckinStatsResponse.ProvinceStatsData> provinces = createCompleteProvinceList(userProvinceGroups);
        
        // 计算已解锁省份数量
        int totalUnlockedProvinces = (int) provinces.stream()
                .filter(ProvinceCheckinStatsResponse.ProvinceStatsData::getIsUnlocked)
                .count();
        
        response.setProvinces(provinces);
        
        // 创建总统计信息
        ProvinceCheckinStatsResponse.OverallStats overallStats = new ProvinceCheckinStatsResponse.OverallStats();
        overallStats.setUnlockedProvinces(totalUnlockedProvinces);
        // 中国34个省级行政区
        overallStats.setTotalProvinces(34);
        overallStats.setCoverageRate(totalUnlockedProvinces * 100.0 / 34);
        
        // 统计用户访问的所有不同博物馆数量
        long totalVisitedMuseums = allRecords.stream()
                .map(CheckinRecord::getMuseumId)
                .distinct()
                .count();
        
        // 从数据库查询全国博物馆总数（活跃且未删除的）
        LambdaQueryWrapper<MuseumInfo> museumWrapper = new LambdaQueryWrapper<>();
        museumWrapper.eq(MuseumInfo::getStatus, 1)
                     .eq(MuseumInfo::getDisplay, 1)
                     .eq(MuseumInfo::getDeleted, false);
        Long totalMuseumsInDB = museumInfoMapper.selectCount(museumWrapper);
        
        overallStats.setVisitedNationalMuseums((int) totalVisitedMuseums);
        overallStats.setTotalNationalMuseums(totalMuseumsInDB != null ? totalMuseumsInDB.intValue() : 130);
        
        response.setOverall(overallStats);
        
        return response;
    }

    /**
     * 从地址中提取省份名称
     */
    private String extractProvinceName(CheckinRecord record) {
        if (record.getAddress() == null || record.getAddress().trim().isEmpty()) {
            return "未知省份";
        }
        
        String address = record.getAddress().trim();
        
        // 直辖市处理（优先匹配完整名称）
        if (address.contains("北京市") || address.contains("北京")) {
            return "北京";
        }
        if (address.contains("上海市") || address.contains("上海")) {
            return "上海";
        }
        if (address.contains("天津市") || address.contains("天津")) {
            return "天津";
        }
        if (address.contains("重庆市") || address.contains("重庆")) {
            return "重庆";
        }
        
        // 自治区处理（优先匹配完整名称）
        if (address.contains("新疆维吾尔自治区") || address.contains("新疆")) {
            return "新疆维吾尔自治区";
        }
        if (address.contains("西藏自治区") || address.contains("西藏")) {
            return "西藏自治区";
        }
        if (address.contains("内蒙古自治区") || address.contains("内蒙古")) {
            return "内蒙古自治区";
        }
        if (address.contains("广西壮族自治区") || address.contains("广西")) {
            return "广西壮族自治区";
        }
        if (address.contains("宁夏回族自治区") || address.contains("宁夏")) {
            return "宁夏回族自治区";
        }
        
        // 特殊行政区
        if (address.contains("香港特别行政区") || address.contains("香港")) {
            return "香港特别行政区";
        }
        if (address.contains("澳门特别行政区") || address.contains("澳门")) {
            return "澳门特别行政区";
        }
        
        // 省份处理 - 精确匹配所有省份
        String[] provinces = {
            "河北省", "山西省", "辽宁省", "吉林省", "黑龙江省", 
            "江苏省", "浙江省", "安徽省", "福建省", "江西省", "山东省",
            "河南省", "湖北省", "湖南省", "广东省", "海南省",
            "四川省", "贵州省", "云南省", "陕西省", "甘肃省", "青海省",
            "台湾省"
        };
        
        // 优先匹配完整省名
        for (String province : provinces) {
            if (address.contains(province)) {
                return province;
            }
        }
        
        // 如果没有匹配到完整省名，尝试去掉"省"字匹配简称
        for (String province : provinces) {
            String shortName = province.replace("省", "");
            if (address.contains(shortName) && !shortName.isEmpty()) {
                return province;
            }
        }
        
        log.warn("无法从地址'{}'中提取省份名称", address);
        return "未知省份";
    }

    /**
     * 创建完整的省份列表（包括未探索的省份）
     */
    private List<ProvinceCheckinStatsResponse.ProvinceStatsData> createCompleteProvinceList(
            Map<String, List<CheckinRecord>> userProvinceGroups) {
        
        List<ProvinceCheckinStatsResponse.ProvinceStatsData> provinces = new ArrayList<>();
        
        // 从数据库查询完整的省份列表
        List<AreaProvince> allProvinces = areaProvinceMapper.selectList(null);
        
        for (AreaProvince areaProvince : allProvinces) {
            String provinceCode = areaProvince.getAdcode();
            String provinceName = areaProvince.getName();
            
            ProvinceCheckinStatsResponse.ProvinceStatsData provinceData = 
                new ProvinceCheckinStatsResponse.ProvinceStatsData();
            
            provinceData.setProvinceCode(provinceCode);
            provinceData.setProvinceName(provinceName);
            
            // 从数据库统计该省份的博物馆总数（通过地址匹配）
            Integer totalMuseums = getProvinceMuseumCountFromDB(provinceName);
            provinceData.setTotalMuseums(totalMuseums);
            
            // 检查用户是否在该省份有打卡记录
            List<CheckinRecord> provinceRecords = userProvinceGroups.get(provinceName);
            
            if (provinceRecords != null && !provinceRecords.isEmpty()) {
                // 已探索的省份
                provinceData.setIsUnlocked(true);
                provinceData.setCheckinCount(provinceRecords.size());
                
                // 按博物馆分组统计
                Map<Long, List<CheckinRecord>> museumGroups = provinceRecords.stream()
                        .collect(Collectors.groupingBy(CheckinRecord::getMuseumId));
                
                provinceData.setVisitedMuseums(museumGroups.size());
                
                // 最后打卡时间
                Optional<CheckinRecord> lastRecord = provinceRecords.stream()
                        .max(Comparator.comparing(CheckinRecord::getCheckinTime));
                if (lastRecord.isPresent()) {
                    provinceData.setLastCheckinTime(lastRecord.get().getCheckinTime().toString());
                }
                
                // 创建已访问博物馆列表
                List<ProvinceCheckinStatsResponse.VisitedMuseum> visitedMuseums = new ArrayList<>();
                for (Map.Entry<Long, List<CheckinRecord>> entry : museumGroups.entrySet()) {
                    List<CheckinRecord> museumRecords = entry.getValue();
                    CheckinRecord firstRecord = museumRecords.get(museumRecords.size() - 1);
                    CheckinRecord lastMuseumRecord = museumRecords.get(0);
                    
                    ProvinceCheckinStatsResponse.VisitedMuseum museum = 
                        new ProvinceCheckinStatsResponse.VisitedMuseum();
                    museum.setId(entry.getKey());
                    museum.setName(firstRecord.getMuseumName());
                    museum.setCheckinCount(museumRecords.size());
                    museum.setFirstCheckinTime(firstRecord.getCheckinTime().toString());
                    museum.setLastCheckinTime(lastMuseumRecord.getCheckinTime().toString());
                    
                    visitedMuseums.add(museum);
                }
                
                provinceData.setVisitedMuseumList(visitedMuseums);
                
            } else {
                // 未探索的省份
                provinceData.setIsUnlocked(false);
                provinceData.setVisitedMuseums(0);
                provinceData.setCheckinCount(0);
                provinceData.setLastCheckinTime(null);
                provinceData.setVisitedMuseumList(new ArrayList<>());
            }
            
            provinces.add(provinceData);
        }
        
        // 按解锁状态和打卡次数排序（已解锁的在前，然后按打卡次数排序）
        provinces.sort((a, b) -> {
            if (a.getIsUnlocked() && !b.getIsUnlocked()) {
                return -1;
            }
            if (!a.getIsUnlocked() && b.getIsUnlocked()) {
                return 1;
            }
            if (a.getIsUnlocked() && b.getIsUnlocked()) {
                return Integer.compare(b.getCheckinCount(), a.getCheckinCount());
            }
            return a.getProvinceName().compareTo(b.getProvinceName());
        });
        
        return provinces;
    }

    /**
     * 从数据库统计省份博物馆总数
     */
    private Integer getProvinceMuseumCountFromDB(String provinceName) {
        log.debug("查询省份{}的博物馆总数", provinceName);
        
        // 根据省份名称获取省份编码
        String provinceCode = getProvinceCodeByName(provinceName);
        if (provinceCode == null) {
            log.warn("未找到省份{}对应的编码，使用地址模糊匹配", provinceName);
            // 如果没有找到编码，回退到地址模糊匹配
            return getMuseumCountByAddressMatch(provinceName);
        }
        
        try {
            // 使用省份编码精确查询博物馆数量
            LambdaQueryWrapper<MuseumInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MuseumInfo::getProvinceCode, provinceCode)
                   // 只统计开放状态的博物馆
                   .eq(MuseumInfo::getStatus, 1);
            
            Long count = museumInfoMapper.selectCount(wrapper);
            Integer result = count.intValue();
            
            log.debug("省份{}(编码:{})共有{}个博物馆", provinceName, provinceCode, result);
            return result;
            
        } catch (Exception e) {
            log.error("查询省份{}博物馆数量失败: {}", provinceName, e.getMessage(), e);
            // 出现异常时，回退到地址模糊匹配
            return getMuseumCountByAddressMatch(provinceName);
        }
    }

    /**
     * 根据省份名称获取省份编码
     */
    private String getProvinceCodeByName(String provinceName) {
        if (provinceName == null || provinceName.trim().isEmpty()) {
            return null;
        }
        
        try {
            // 从数据库查询省份编码
            LambdaQueryWrapper<AreaProvince> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AreaProvince::getName, provinceName.trim());
            
            AreaProvince province = areaProvinceMapper.selectOne(wrapper);
            if (province != null) {
                return province.getAdcode();
            }
            
            log.warn("未找到省份{}对应的编码", provinceName);
            return null;
            
        } catch (Exception e) {
            log.error("查询省份编码失败，provinceName: {}, 错误: {}", provinceName, e.getMessage());
            return null;
        }
    }

    /**
     * 通过地址模糊匹配统计博物馆数量（备用方案）
     */
    private Integer getMuseumCountByAddressMatch(String provinceName) {
        log.debug("使用地址模糊匹配查询省份{}的博物馆数量", provinceName);
        
        try {
            LambdaQueryWrapper<MuseumInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(MuseumInfo::getAddress, provinceName)
                   // 只统计开放状态的博物馆
                   .eq(MuseumInfo::getStatus, 1);
            
            Long count = museumInfoMapper.selectCount(wrapper);
            Integer result = count.intValue();
            
            log.debug("省份{}通过地址匹配找到{}个博物馆", provinceName, result);
            return result;
            
        } catch (Exception e) {
            log.error("通过地址匹配查询省份{}博物馆数量失败: {}", provinceName, e.getMessage(), e);
            // 最后的备用方案：返回默认值
            return getDefaultProvinceMuseumCount(provinceName);
        }
    }

    /**
     * 获取省份博物馆默认数量（最后备用方案）
     */
    private Integer getDefaultProvinceMuseumCount(String provinceName) {
        // 数据库查询失败时，返回0而非硬编码估算值
        log.warn("无法获取省份{}的博物馆数量，返回默认值0", provinceName);
        return 0;
    }

    @Override
    public ProvinceMuseumDetailResponse getProvinceMuseumDetail(String provinceCode, Long userId) {
        ProvinceMuseumDetailResponse response = new ProvinceMuseumDetailResponse();
        
        // 根据省份编码获取省份名称
        String provinceName = getProvinceNameByCode(provinceCode);
        response.setProvinceCode(provinceCode);
        response.setProvinceName(provinceName);
        
        // 查询该省份的所有博物馆
        LambdaQueryWrapper<MuseumInfo> museumWrapper = new LambdaQueryWrapper<>();
        museumWrapper.eq(MuseumInfo::getProvinceCode, provinceCode)
                    // 只查询开放状态的博物馆
                    .eq(MuseumInfo::getStatus, 1)
                    // 按等级排序，高等级优先
                    .orderByDesc(MuseumInfo::getLevel)
                    .orderByAsc(MuseumInfo::getName);
        
        List<MuseumInfo> museums = museumInfoMapper.selectList(museumWrapper);
        
        // 查询用户在该省份的打卡记录
        LambdaQueryWrapper<CheckinRecord> checkinWrapper = new LambdaQueryWrapper<>();
        checkinWrapper.eq(CheckinRecord::getUserId, userId)
                     .eq(CheckinRecord::getIsDraft, false)
                     .eq(CheckinRecord::getDeleted, false);
        
        List<CheckinRecord> userCheckins = checkinRecordMapper.selectList(checkinWrapper);
        
        // 按博物馆ID分组打卡记录
        Map<Long, List<CheckinRecord>> checkinByMuseum = userCheckins.stream()
                .collect(Collectors.groupingBy(CheckinRecord::getMuseumId));
        
        // 构建博物馆详情列表
        List<ProvinceMuseumDetailResponse.MuseumDetailInfo> museumDetails = new ArrayList<>();
        int visitedCount = 0;
        
        for (MuseumInfo museum : museums) {
            ProvinceMuseumDetailResponse.MuseumDetailInfo detail = 
                new ProvinceMuseumDetailResponse.MuseumDetailInfo();
            
            // 基本信息
            detail.setId(museum.getId());
            detail.setName(museum.getName());
            detail.setAddress(museum.getAddress());
            detail.setOpenTime(museum.getOpenTime());
            detail.setDescription(museum.getDescription());
            detail.setStatus(museum.getStatus());
            detail.setCityName(getCityNameByCode(museum.getCityCode()));
            
            // 门票信息
            if (museum.getTicketPrice() != null) {
                detail.setTicketPrice(museum.getTicketPrice().intValue());
            }
            detail.setFreeAdmission(museum.getFreeAdmission());
            
            // 等级信息转换
            detail.setLevel(convertLevelToString(museum.getLevel()));
            // 使用type字段作为category
            detail.setCategory(museum.getType());
            
            // 检查用户是否访问过这个博物馆
            List<CheckinRecord> museumCheckins = checkinByMuseum.get(museum.getId());
            if (museumCheckins != null && !museumCheckins.isEmpty()) {
                detail.setIsVisited(true);
                detail.setVisitCount(museumCheckins.size());
                visitedCount++;
                
                // 首次和最后访问时间
                Optional<CheckinRecord> firstVisit = museumCheckins.stream()
                        .min(Comparator.comparing(CheckinRecord::getCheckinTime));
                Optional<CheckinRecord> lastVisit = museumCheckins.stream()
                        .max(Comparator.comparing(CheckinRecord::getCheckinTime));
                
                if (firstVisit.isPresent()) {
                    detail.setFirstVisitDate(firstVisit.get().getCheckinTime().toString());
                }
                if (lastVisit.isPresent()) {
                    detail.setLastVisitDate(lastVisit.get().getCheckinTime().toString());
                }
            } else {
                detail.setIsVisited(false);
                detail.setVisitCount(0);
            }
            
            museumDetails.add(detail);
        }
        
        // 按城市分组博物馆，生成城市统计数据
        List<ProvinceMuseumDetailResponse.CityStatsInfo> cityStats = generateCityStats(museums, checkinByMuseum);
        
        // 设置统计信息
        response.setTotalMuseums(museums.size());
        response.setVisitedMuseums(visitedCount);
        response.setMuseums(museumDetails);
        response.setCities(cityStats);
        
        return response;
    }

    /**
     * 生成城市统计数据
     */
    private List<ProvinceMuseumDetailResponse.CityStatsInfo> generateCityStats(
            List<MuseumInfo> museums, Map<Long, List<CheckinRecord>> checkinByMuseum) {
        
        log.debug("开始生成城市统计数据，共{}个博物馆", museums.size());
        
        // 按城市分组博物馆
        Map<String, List<MuseumInfo>> cityGroups = museums.stream()
                .collect(Collectors.groupingBy(museum -> {
                    String cityName = getCityNameByCode(museum.getCityCode());
                    return cityName != null && !cityName.isEmpty() ? cityName : "未知城市";
                }));
        
        List<ProvinceMuseumDetailResponse.CityStatsInfo> cityStats = new ArrayList<>();
        
        for (Map.Entry<String, List<MuseumInfo>> entry : cityGroups.entrySet()) {
            String cityName = entry.getKey();
            List<MuseumInfo> cityMuseums = entry.getValue();
            
            // 统计该城市的打卡情况
            int totalMuseums = cityMuseums.size();
            int visitedMuseums = 0;
            int totalCheckins = 0;
            String lastCheckinTime = null;
            
            // 遍历该城市的博物馆，统计打卡数据
            for (MuseumInfo museum : cityMuseums) {
                List<CheckinRecord> museumCheckins = checkinByMuseum.get(museum.getId());
                if (museumCheckins != null && !museumCheckins.isEmpty()) {
                    visitedMuseums++;
                    totalCheckins += museumCheckins.size();
                    
                    // 找到最新的打卡时间
                    Optional<CheckinRecord> latestRecord = museumCheckins.stream()
                            .max(Comparator.comparing(CheckinRecord::getCheckinTime));
                    if (latestRecord.isPresent()) {
                        String recordTime = latestRecord.get().getCheckinTime().toString();
                        if (lastCheckinTime == null || recordTime.compareTo(lastCheckinTime) > 0) {
                            lastCheckinTime = recordTime;
                        }
                    }
                }
            }
            
            // 计算完成度
            int completionRate = totalMuseums > 0 ? (visitedMuseums * 100 / totalMuseums) : 0;
            
            // 构建城市统计信息
            ProvinceMuseumDetailResponse.CityStatsInfo cityStatsInfo = 
                new ProvinceMuseumDetailResponse.CityStatsInfo();
            cityStatsInfo.setCityName(cityName);
            cityStatsInfo.setTotalMuseums(totalMuseums);
            cityStatsInfo.setVisitedMuseums(visitedMuseums);
            cityStatsInfo.setCompletionRate(completionRate);
            cityStatsInfo.setIsUnlocked(visitedMuseums > 0);
            cityStatsInfo.setLastCheckinTime(lastCheckinTime);
            cityStatsInfo.setCheckinCount(totalCheckins);
            
            cityStats.add(cityStatsInfo);
            
            log.debug("城市{}统计：{}个博物馆，已访问{}个，完成度{}%", 
                    cityName, totalMuseums, visitedMuseums, completionRate);
        }
        
        // 按解锁状态和访问数量排序（已解锁的在前，然后按访问数量排序）
        cityStats.sort((a, b) -> {
            if (a.getIsUnlocked() && !b.getIsUnlocked()) {
                return -1;
            }
            if (!a.getIsUnlocked() && b.getIsUnlocked()) {
                return 1;
            }
            if (a.getIsUnlocked() && b.getIsUnlocked()) {
                return Integer.compare(b.getVisitedMuseums(), a.getVisitedMuseums());
            }
            return a.getCityName().compareTo(b.getCityName());
        });
        
        log.debug("城市统计数据生成完成，共{}个城市", cityStats.size());
        return cityStats;
    }

    /**
     * 根据省份编码获取省份名称
     */
    private String getProvinceNameByCode(String provinceCode) {
        if (provinceCode == null || provinceCode.trim().isEmpty()) {
            return "未知省份";
        }
        
        try {
            // 从数据库查询省份名称
            LambdaQueryWrapper<AreaProvince> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AreaProvince::getAdcode, provinceCode);
            
            AreaProvince province = areaProvinceMapper.selectOne(wrapper);
            if (province != null) {
                return province.getName();
            }
            
            log.warn("未找到省份编码{}对应的省份名称", provinceCode);
            return "未知省份";
            
        } catch (Exception e) {
            log.error("查询省份名称失败，provinceCode: {}, 错误: {}", provinceCode, e.getMessage());
            return "未知省份";
        }
    }

    /**
     * 根据城市编码获取城市名称（从数据库查询优先）
     */
    private String getCityNameByCode(String cityCode) {
        if (cityCode == null || cityCode.trim().isEmpty()) {
            return "未知城市";
        }
        
        try {
            // 首先从数据库查询城市名称
            LambdaQueryWrapper<AreaCity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(AreaCity::getAdcode, cityCode);
            
            AreaCity city = areaCityMapper.selectOne(wrapper);
            if (city != null) {
                log.debug("从数据库查询到城市：{} -> {}", cityCode, city.getName());
                return city.getName();
            }
            
            // 数据库查询失败，尝试从博物馆地址中解析城市名称
            log.debug("数据库未找到城市编码{}，尝试从地址解析", cityCode);
            String cityNameFromAddress = getCityNameFromAddress(cityCode);
            if (cityNameFromAddress != null) {
                return cityNameFromAddress;
            }
            
            // 所有方法都失败，返回未知城市
            log.warn("无法获取城市编码{}对应的城市名称", cityCode);
            return "未知城市";
            
        } catch (Exception e) {
            log.warn("获取城市名称失败，cityCode: {}, 错误: {}", cityCode, e.getMessage());
            return "未知城市";
        }
    }
    
    /**
     * 通过城市编码映射获取城市名称（已废弃硬编码方式）
     */
    private String getCityNameFromMapping(String cityCode) {
        // 不再使用硬编码映射，直接返回null让后续处理
        log.debug("城市编码映射表已废弃，cityCode: {}", cityCode);
        return null;
    }
    
    /**
     * 通过查询该城市编码的博物馆地址来推断城市名称
     */
    private String getCityNameFromAddress(String cityCode) {
        try {
            // 查询该城市编码下的博物馆，从地址中提取城市名称
            LambdaQueryWrapper<MuseumInfo> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(MuseumInfo::getCityCode, cityCode)
                   .eq(MuseumInfo::getStatus, 1)
                   .isNotNull(MuseumInfo::getAddress)
                   .last("LIMIT 1");
            
            MuseumInfo museum = museumInfoMapper.selectOne(wrapper);
            if (museum != null && museum.getAddress() != null) {
                return extractCityNameFromAddress(museum.getAddress());
            }
        } catch (Exception e) {
            log.warn("从地址解析城市名称失败，cityCode: {}", cityCode);
        }
        
        return null;
    }
    
    /**
     * 从地址中提取城市名称
     */
    private String extractCityNameFromAddress(String address) {
        if (address == null || address.trim().isEmpty()) {
            return "未知城市";
        }
        
        // 移除省份部分，尝试提取城市名称
        String[] parts = address.split("[省市区县]");
        if (parts.length >= 2) {
            String cityPart = parts[1].trim();
            if (!cityPart.isEmpty()) {
                return cityPart + "市";
            }
        }
        
        // 如果解析失败，返回默认值
        return "未知城市";
    }

    /**
     * 将数字等级转换为字符串描述
     */
    private String convertLevelToString(Integer level) {
        if (level == null) {
            return "未定级";
        }
        
        switch (level) {
            case 1: return "国家一级";
            case 2: return "国家二级";
            case 3: return "国家三级";
            case 4: return "省级";
            case 5: return "市级";
            default: return "专业级";
        }
    }

    /**
     * 根据地区名称获取该地区的博物馆ID列表
     * 优先级搜索：省份 → 城市 → 博物馆名称
     * @param regionName 地区名称（省份或城市）
     * @return 博物馆ID列表
     */
    private List<Long> getMuseumIdsByRegionName(String regionName) {
        try {
            // 第一优先级：查找匹配的省份
            LambdaQueryWrapper<AreaProvince> provinceWrapper = new LambdaQueryWrapper<>();
            provinceWrapper.like(AreaProvince::getName, regionName);
            List<AreaProvince> provinces = areaProvinceMapper.selectList(provinceWrapper);
            
            if (!provinces.isEmpty()) {
                String provinceCode = provinces.get(0).getAdcode();
                return getMuseumIdsByProvinceCode(provinceCode, regionName);
            }
            
            // 第二优先级：查找匹配的城市
            LambdaQueryWrapper<AreaCity> cityWrapper = new LambdaQueryWrapper<>();
            cityWrapper.like(AreaCity::getName, regionName);
            List<AreaCity> cities = areaCityMapper.selectList(cityWrapper);
            
            if (!cities.isEmpty()) {
                String cityCode = cities.get(0).getAdcode();
                return getMuseumIdsByCityCode(cityCode, regionName);
            }
            
            // 第三优先级：没有找到地区匹配，返回空列表（后续会使用博物馆名称模糊搜索）
            return new ArrayList<>();
            
        } catch (Exception e) {
            log.error("根据地区名称查找博物馆失败: {}", regionName, e);
            return new ArrayList<>();
        }
    }

    /**
     * 根据省份代码查找博物馆ID列表
     */
    private List<Long> getMuseumIdsByProvinceCode(String provinceCode, String searchTerm) {
        LambdaQueryWrapper<MuseumInfo> museumWrapper = new LambdaQueryWrapper<>();
        museumWrapper.likeRight(MuseumInfo::getProvinceCode, provinceCode)
                    .eq(MuseumInfo::getStatus, 1)
                    .eq(MuseumInfo::getDisplay, 1)
                    .eq(MuseumInfo::getDeleted, false);
        
        List<MuseumInfo> museums = museumInfoMapper.selectList(museumWrapper);
        List<Long> museumIds = museums.stream()
                .map(MuseumInfo::getId)
                .collect(Collectors.toList());
        
        return museumIds;
    }

    /**
     * 根据城市代码查找博物馆ID列表
     */
    private List<Long> getMuseumIdsByCityCode(String cityCode, String searchTerm) {
        LambdaQueryWrapper<MuseumInfo> museumWrapper = new LambdaQueryWrapper<>();
        museumWrapper.likeRight(MuseumInfo::getCityCode, cityCode)
                    .eq(MuseumInfo::getStatus, 1)
                    .eq(MuseumInfo::getDisplay, 1)
                    .eq(MuseumInfo::getDeleted, false);
        
        List<MuseumInfo> museums = museumInfoMapper.selectList(museumWrapper);
        List<Long> museumIds = museums.stream()
                .map(MuseumInfo::getId)
                .collect(Collectors.toList());
        
        return museumIds;
    }
}
