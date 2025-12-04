package com.lynn.museum.info.entity;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 成就实体
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("achievements")
public class Achievement extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 成就ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 成就唯一标识符
     */
    @TableField("achievement_key")
    private String achievementKey;

    /**
     * 成就名称
     */
    @TableField("name")
    private String name;

    /**
     * 成就描述
     */
    @TableField("description")
    private String description;

    /**
     * 成就图标
     */
    @TableField("icon")
    private String icon;

    /**
     * 成就分类：checkin-打卡成就，explore-探索成就，social-社交成就，special-特殊成就
     */
    @TableField("category")
    private String category;

    /**
     * 解锁要求描述
     */
    @TableField("requirement")
    private String requirement;

    /**
     * 目标数值
     */
    @TableField("target")
    private Integer target;

    /**
     * 稀有度：common-普通，rare-稀有，epic-史诗，legendary-传奇
     */
    @TableField("rarity")
    private String rarity;

    /**
     * 成就类型：count-计数类型，milestone-里程碑类型，condition-条件类型
     */
    @TableField("type")
    private String type;

    /**
     * 排序权重
     */
    @TableField("sort_order")
    private Integer sortOrder;

    /**
     * 状态：0-禁用，1-启用
     */
    @TableField("status")
    private Integer status;

    /**
     * 是否删除：0-未删除，1-已删除
     */
    @TableField("deleted")
    @TableLogic
    private Integer deleted;
}
