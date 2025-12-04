package com.lynn.museum.info.model.entity;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDate;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 博物馆展览实体类
 *
 * @author lynn
 * @since 2024-01-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("museum_exhibition")
public class MuseumExhibition extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 展览ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    /**
     * 博物馆ID
     */
    private Long museumId;

    /**
     * 展览标题
     */
    private String title;

    /**
     * 展览描述
     */
    private String description;

    /**
     * 封面图片URL
     */
    private String coverImage;

    /**
     * 开始日期
     */
    private LocalDate startDate;

    /**
     * 结束日期
     */
    private LocalDate endDate;

    /**
     * 展厅位置
     */
    private String location;

    /**
     * 门票价格
     */
    private BigDecimal ticketPrice;

    /**
     * 状态：0-已结束，1-进行中，2-未开始
     */
    private Integer status;

    /**
     * 是否常设展览：0-临时展览，1-常设展览
     */
    private Integer isPermanent;

    /**
     * 是否展示：0-不展示，1-展示
     */
    private Integer display;
}
