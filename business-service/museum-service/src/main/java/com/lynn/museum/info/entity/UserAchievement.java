package com.lynn.museum.info.entity;

import com.lynn.museum.common.entity.BaseEntity;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 用户成就关联实体
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@TableName("user_achievements")
public class UserAchievement extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    private static final long serialVersionUID = 1L;
/**
     * 用户ID
     */
    @TableField("user_id")
    private Long userId;

    /**
     * 成就ID
     */
    @TableField("achievement_id")
    private Long achievementId;

    /**
     * 当前进度
     */
    @TableField("progress")
    private Integer progress;

    /**
     * 是否已解锁：0-未解锁，1-已解锁
     */
    @TableField("unlocked")
    private Integer unlocked;

    /**
     * 解锁时间
     */
    @TableField("unlocked_time")
    private Date unlockedTime;

    /**
     * 是否已分享：0-未分享，1-已分享
     */
    @TableField("shared")
    private Integer shared;

    /**
     * 分享时间
     */
    @TableField("shared_time")
    private Date sharedTime;
}
