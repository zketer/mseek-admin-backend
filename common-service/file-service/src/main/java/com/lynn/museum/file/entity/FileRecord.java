package com.lynn.museum.file.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.lynn.museum.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 文件记录实体
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("file_records")
public class FileRecord extends BaseEntity {

    /**
     * 文件ID
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableLogic
    @Schema(description = "删除标志：0-未删除，1-已删除", example = "0")
    private Integer deleted;


    /**
     * 文件原始名称
     */
    private String originalName;

    /**
     * 存储文件名（包含路径）
     */
    private String fileName;

    /**
     * 文件大小（字节）
     */
    private Long fileSize;

    /**
     * 文件类型
     */
    private String contentType;

    /**
     * 存储桶名称
     */
    private String bucketName;

    /**
     * 文件分类
     */
    private String category;

    /**
     * 上传者ID
     */
    private Long uploaderId;

    /**
     * 文件状态：0=临时，1=正常，2=已删除
     */
    private Integer status;

    /**
     * 访问次数
     */
    private Integer accessCount;

    /**
     * MD5哈希值
     */
    private String md5Hash;

    /**
     * 备注
     */
    private String remark;
}
