package com.lynn.museum.info.model.entity;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 博物馆标签实体类
 *
 * @author lynn
 * @since 2024-01-01
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("museum_tag")
public class MuseumTag extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 标签ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    /**
     * 标签名称
     */
    private String name;

    /**
     * 标签编码
     */
    private String code;

    /**
     * 标签描述
     */
    private String description;

    /**
     * 标签颜色
     */
    private String color;
}
