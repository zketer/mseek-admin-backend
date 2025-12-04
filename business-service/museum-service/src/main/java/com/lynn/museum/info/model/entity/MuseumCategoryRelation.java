package com.lynn.museum.info.model.entity;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 博物馆与分类关联实体类
 *
 * @author lynn
 * @since 2024-01-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("museum_category_relation")
public class MuseumCategoryRelation extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    private static final long serialVersionUID = 1L;
/**
     * 博物馆ID
     */
    private Long museumId;

    /**
     * 分类ID
     */
    private Long categoryId;
}
