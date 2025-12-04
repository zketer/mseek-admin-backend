package com.lynn.museum.system.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lynn.museum.system.dto.SystemOverviewResponse;
import com.lynn.museum.system.mapper.*;
import com.lynn.museum.system.model.entity.*;
import com.lynn.museum.system.service.SystemOverviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统概览服务实现
 *
 * @author lynn
 * @since 2025-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemOverviewServiceImpl implements SystemOverviewService {

    private final SystemOverviewMapper systemOverviewMapper;
    private final TechStackItemMapper techStackItemMapper;
    private final MicroserviceInfoMapper microserviceInfoMapper;
    private final FeatureModuleMapper featureModuleMapper;
    private final DevelopmentPlanMapper developmentPlanMapper;
    private final ThirdPartyServiceMapper thirdPartyServiceMapper;

    @Override
    public SystemOverviewResponse getSystemOverview() {
        SystemOverviewResponse response = new SystemOverviewResponse();

        // 获取系统基本信息（只取第一条）
        SystemOverview overview = systemOverviewMapper.selectOne(
                new LambdaQueryWrapper<SystemOverview>()
                        .eq(SystemOverview::getDeleted, 0)
                        .last("LIMIT 1")
        );
        if (overview != null) {
            SystemOverviewResponse.SystemBasicInfo basicInfo = new SystemOverviewResponse.SystemBasicInfo();
            basicInfo.setSystemName(overview.getSystemName());
            basicInfo.setSystemVersion(overview.getSystemVersion());
            basicInfo.setArchitectureMode(overview.getArchitectureMode());
            basicInfo.setDeploymentMethod(overview.getDeploymentMethod());
            basicInfo.setTechStack(overview.getTechStack());
            basicInfo.setDataStorage(overview.getDataStorage());
            basicInfo.setServiceGovernance(overview.getServiceGovernance());
            basicInfo.setAuthSolution(overview.getAuthSolution());
            basicInfo.setSystemDescription(overview.getSystemDescription());
            basicInfo.setStatusMessage(overview.getStatusMessage());
            response.setBasicInfo(basicInfo);
        }

        // 获取技术栈列表
        List<TechStackItem> techStackItems = techStackItemMapper.selectList(
                new LambdaQueryWrapper<TechStackItem>()
                        .eq(TechStackItem::getDeleted, 0)
                        .eq(TechStackItem::getStatus, "active")
                        .orderByAsc(TechStackItem::getOrderIndex)
        );
        List<SystemOverviewResponse.TechStackInfo> techStackInfoList = techStackItems.stream()
                .map(item -> {
                    SystemOverviewResponse.TechStackInfo info = new SystemOverviewResponse.TechStackInfo();
                    info.setCategory(item.getCategory());
                    info.setName(item.getName());
                    info.setVersion(item.getVersion());
                    info.setDescription(item.getDescription());
                    info.setTagColor(item.getTagColor());
                    info.setPort(item.getPort());
                    return info;
                })
                .collect(Collectors.toList());
        response.setTechStack(techStackInfoList);

        // 获取微服务列表
        List<MicroserviceInfo> microservices = microserviceInfoMapper.selectList(
                new LambdaQueryWrapper<MicroserviceInfo>()
                        .eq(MicroserviceInfo::getDeleted, 0)
                        .orderByAsc(MicroserviceInfo::getOrderIndex)
        );
        List<SystemOverviewResponse.MicroserviceInfo> microserviceInfoList = microservices.stream()
                .map(ms -> {
                    SystemOverviewResponse.MicroserviceInfo info = new SystemOverviewResponse.MicroserviceInfo();
                    info.setServiceName(ms.getServiceName());
                    info.setServiceCode(ms.getServiceCode());
                    info.setPort(ms.getPort());
                    info.setDescription(ms.getDescription());
                    if (ms.getFeatures() != null) {
                        info.setFeatures(JSONUtil.toList(ms.getFeatures(), String.class));
                    }
                    info.setStatus(ms.getStatus());
                    info.setStatusTagColor(ms.getStatusTagColor());
                    return info;
                })
                .collect(Collectors.toList());
        response.setMicroservices(microserviceInfoList);

        // 获取功能模块列表
        List<FeatureModule> featureModules = featureModuleMapper.selectList(
                new LambdaQueryWrapper<FeatureModule>()
                        .eq(FeatureModule::getDeleted, 0)
                        .orderByAsc(FeatureModule::getOrderIndex)
        );
        List<SystemOverviewResponse.FeatureModuleInfo> featureModuleInfoList = featureModules.stream()
                .map(fm -> {
                    SystemOverviewResponse.FeatureModuleInfo info = new SystemOverviewResponse.FeatureModuleInfo();
                    info.setModuleName(fm.getModuleName());
                    info.setModuleType(fm.getModuleType());
                    info.setDescription(fm.getDescription());
                    info.setProgress(fm.getProgress());
                    info.setTagText(fm.getTagText());
                    info.setTagColor(fm.getTagColor());
                    return info;
                })
                .collect(Collectors.toList());
        response.setFeatureModules(featureModuleInfoList);

        // 获取开发计划列表
        List<DevelopmentPlan> developmentPlans = developmentPlanMapper.selectList(
                new LambdaQueryWrapper<DevelopmentPlan>()
                        .eq(DevelopmentPlan::getDeleted, 0)
                        .orderByAsc(DevelopmentPlan::getOrderIndex)
        );
        List<SystemOverviewResponse.DevelopmentPlanInfo> developmentPlanInfoList = developmentPlans.stream()
                .map(dp -> {
                    SystemOverviewResponse.DevelopmentPlanInfo info = new SystemOverviewResponse.DevelopmentPlanInfo();
                    info.setPlanType(dp.getPlanType());
                    info.setTitle(dp.getTitle());
                    if (dp.getItems() != null) {
                        info.setItems(JSONUtil.toList(dp.getItems(), String.class));
                    }
                    return info;
                })
                .collect(Collectors.toList());
        response.setDevelopmentPlans(developmentPlanInfoList);

        // 获取第三方服务列表
        List<ThirdPartyService> thirdPartyServices = thirdPartyServiceMapper.selectList(
                new LambdaQueryWrapper<ThirdPartyService>()
                        .eq(ThirdPartyService::getDeleted, 0)
                        .orderByAsc(ThirdPartyService::getOrderIndex)
        );
        List<SystemOverviewResponse.ThirdPartyServiceInfo> thirdPartyServiceInfoList = thirdPartyServices.stream()
                .map(tps -> {
                    SystemOverviewResponse.ThirdPartyServiceInfo info = new SystemOverviewResponse.ThirdPartyServiceInfo();
                    info.setServiceName(tps.getServiceName());
                    info.setServiceType(tps.getServiceType());
                    info.setStatus(tps.getStatus());
                    info.setStatusTagColor(tps.getStatusTagColor());
                    info.setDescription(tps.getDescription());
                    return info;
                })
                .collect(Collectors.toList());
        response.setThirdPartyServices(thirdPartyServiceInfoList);

        return response;
    }
}

