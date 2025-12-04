package com.lynn.museum.info.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 轮播图实体类
 *
 * @author lynn
 * @since 2024-12-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("t_banner")
public class Banner extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 轮播图ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    /**
     * 轮播图标题
     */
    private String title;

    /**
     * 图片URL
     */
    private String imageUrl;

    /**
     * 链接类型：museum/exhibition/external/none
     */
    private String linkType;

    /**
     * 链接值
     */
    private String linkValue;

    /**
     * 排序权重
     */
    private Integer sort;

    /**
     * 状态：0-下线，1-上线
     */
    private Integer status;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 点击次数
     */
    private Integer clickCount;
}
