package com.lynn.museum.system.model.entity;

import com.lynn.museum.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 部门实体类
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_department")
public class Department extends BaseEntity {

    @TableId(type = IdType.AUTO)
    @Schema(description = "主键ID")
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    /**
     * 部门名称
     */
    private String deptName;

    /**
     * 部门编码
     */
    private String deptCode;

    /**
     * 父级部门ID
     */
    private Long parentId;

    /**
     * 祖级列表（用逗号分隔）
     */
    private String ancestors;

    /**
     * 部门负责人
     */
    private String leader;

    /**
     * 负责人手机号
     */
    private String leaderPhone;

    /**
     * 负责人邮箱
     */
    private String leaderEmail;

    /**
     * 显示顺序
     */
    private Integer orderNum;

    /**
     * 状态：0-停用，1-正常
     */
    private Integer status;

    /**
     * 部门描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

}
