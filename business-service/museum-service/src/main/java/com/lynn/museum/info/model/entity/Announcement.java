package com.lynn.museum.info.model.entity;

import java.util.Date;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableLogic;

/**
 * 公告实体类
 */
@EqualsAndHashCode(callSuper = true)
@Data
@TableName("t_announcement")
@Schema(description = "公告")
public class Announcement extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    @Schema(description = "公告标题")
    private String title;

    @Schema(description = "公告内容")
    private String content;

    @Schema(description = "公告类型：general/maintenance/activity")
    private String type;

    @Schema(description = "优先级：0-普通，1-重要，2-紧急")
    private Integer priority;

    @Schema(description = "状态：0-草稿，1-发布，2-下线")
    private Integer status;

    @Schema(description = "发布时间")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date publishTime;

    @Schema(description = "过期时间")
    @TableField(updateStrategy = FieldStrategy.IGNORED)
    private Date expireTime;

    @Schema(description = "阅读次数")
    private Integer readCount;

    @Schema(description = "创建人ID")
    private Long createUserId;

    @Schema(description = "是否启用")
    private Integer enabled;
}
