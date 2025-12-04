package com.lynn.museum.system.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lynn.museum.system.dto.SystemArchitectureResponse;
import com.lynn.museum.system.mapper.SystemArchitectureMapper;
import com.lynn.museum.system.model.entity.SystemArchitecture;
import com.lynn.museum.system.service.SystemArchitectureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 系统架构服务实现
 *
 * @author lynn
 * @since 2025-01-20
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemArchitectureServiceImpl implements SystemArchitectureService {

    private final SystemArchitectureMapper systemArchitectureMapper;

    @Override
    public SystemArchitectureResponse getSystemArchitecture() {
        SystemArchitectureResponse response = new SystemArchitectureResponse();

        // 获取所有激活的架构信息
        List<SystemArchitecture> architectures = systemArchitectureMapper.selectList(
                new LambdaQueryWrapper<SystemArchitecture>()
                        .eq(SystemArchitecture::getDeleted, 0)
                        .eq(SystemArchitecture::getStatus, "active")
                        .orderByAsc(SystemArchitecture::getOrderIndex)
        );

        List<SystemArchitectureResponse.ArchitectureInfo> architectureInfoList = architectures.stream()
                .map(arch -> {
                    SystemArchitectureResponse.ArchitectureInfo info = new SystemArchitectureResponse.ArchitectureInfo();
                    info.setId(arch.getId());
                    info.setType(arch.getType());
                    info.setTitle(arch.getTitle());
                    info.setMermaidCode(arch.getMermaidCode());
                    info.setDescription(arch.getDescription());
                    if (arch.getArchitectureDetails() != null) {
                        info.setArchitectureDetails(JSONUtil.parseObj(arch.getArchitectureDetails()));
                    }
                    info.setOrderIndex(arch.getOrderIndex());
                    info.setStatus(arch.getStatus());
                    return info;
                })
                .collect(Collectors.toList());

        response.setArchitectures(architectureInfoList);
        return response;
    }
}

