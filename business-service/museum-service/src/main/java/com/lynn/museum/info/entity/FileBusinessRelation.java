package com.lynn.museum.info.entity;

import java.util.Date;

import com.lynn.museum.common.entity.BaseEntity;

import com.baomidou.mybatisplus.annotation.*;
import com.lynn.museum.info.enums.BusinessTypeEnum;
import com.lynn.museum.info.enums.RelationTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 文件业务关联实体
 * 用于关联文件和各种业务实体（博物馆、展览、横幅、公告等）
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_business_relation")
@Schema(name = "FileBusinessRelation", description = "文件业务关联实体")
public class FileBusinessRelation extends BaseEntity {

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    /**
     * 文件ID，关联file表
     */
    @TableField("file_id")
    @Schema(description = "文件ID，关联file表")
    private Long fileId;

    /**
     * 业务实体ID
     */
    @TableField("business_id")
    @Schema(description = "业务实体ID")
    private Long businessId;

    /**
     * 业务类型
     */
    @TableField("business_type")
    @Schema(description = "业务类型：MUSEUM-博物馆, EXHIBITION-展览, BANNER-横幅, ANNOUNCEMENT-公告, RECOMMENDATION-推荐")
    private BusinessTypeEnum businessType;

    /**
     * 关系类型
     */
    @TableField("relation_type")
    @Schema(description = "关系类型：MAIN_IMAGE-主图, THUMBNAIL-缩略图, GALLERY-图库, DOCUMENT-文档, AVATAR-头像")
    private RelationTypeEnum relationType;

    /**
     * 排序字段，数字越小排序越前
     */
    @TableField("sort_order")
    @Schema(description = "排序字段，数字越小排序越前")
    private Integer sortOrder;

    /**
     * 状态：0=禁用, 1=启用
     */
    @TableField("status")
    @Schema(description = "状态：0=禁用, 1=启用")
    private Integer status;

    /**
     * 备注说明
     */
    @TableField("remark")
    @Schema(description = "备注说明")
    private String remark;

    /**
     * 逻辑删除：0=未删除, 1=已删除
     */
    @TableField("deleted")
    @TableLogic
    @Schema(description = "逻辑删除：0=未删除, 1=已删除")
    private Integer deleted;
}
