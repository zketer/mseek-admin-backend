package com.lynn.museum.info.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.lynn.museum.common.result.Result;
import com.lynn.museum.info.dto.ExhibitionResponse;
import com.lynn.museum.info.service.MuseumExhibitionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 小程序展览控制器
 *
 * @author lynn
 * @since 2024-01-01
 */
@Tag(name = "MiniAppExhibitionController", description = "小程序展览相关接口")
@RestController
@RequestMapping("/miniapp/exhibitions")
@RequiredArgsConstructor
@Slf4j
public class MiniAppExhibitionController {

    private final MuseumExhibitionService museumExhibitionService;

    @Operation(summary = "分页获取最新展览列表", description = "获取正在进行或即将开始的展览，按开始时间排序，支持懒加载")
    @GetMapping("/latest")
    public Result<IPage<ExhibitionResponse>> getLatestExhibitions(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") Integer pageSize) {

        log.info("小程序分页获取最新展览列表，页码：{}，页面大小：{}", page, pageSize);

        IPage<ExhibitionResponse> exhibitions = museumExhibitionService.getLatestExhibitions(page, pageSize);
        return Result.success(exhibitions);
    }

    @Operation(summary = "分页获取所有展览列表", description = "支持多种过滤条件的展览分页查询，支持懒加载")
    @GetMapping("/all")
    public Result<IPage<ExhibitionResponse>> getAllExhibitions(
            @Parameter(description = "当前页") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "页面大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "博物馆ID（可选）") @RequestParam(required = false) Long museumId,
            @Parameter(description = "展览标题搜索（可选）") @RequestParam(required = false) String title,
            @Parameter(description = "状态：0-已结束，1-进行中，2-未开始（可选）") @RequestParam(required = false) Integer status,
            @Parameter(description = "是否常设展览：0-临时展览，1-常设展览（可选）") @RequestParam(required = false) Integer isPermanent) {

        log.info("小程序分页查询所有展览列表 - 页码：{}, 大小：{}, 博物馆ID：{}, 标题：{}, 状态：{}, 常设：{}",
                page, pageSize, museumId, title, status, isPermanent);

        IPage<ExhibitionResponse> exhibitions = museumExhibitionService.getAllExhibitions(page, pageSize, museumId, title, status, isPermanent);
        return Result.success(exhibitions);
    }

    @Operation(summary = "获取展览详情", description = "获取展览详细信息")
    @GetMapping("/{id}")
    public Result<ExhibitionResponse> getExhibitionDetail(
            @Parameter(description = "展览ID") @PathVariable Long id) {

        log.info("小程序获取展览详情，ID：{}", id);

        ExhibitionResponse exhibition = museumExhibitionService.getExhibitionById(id);
        return Result.success(exhibition);
    }
}
