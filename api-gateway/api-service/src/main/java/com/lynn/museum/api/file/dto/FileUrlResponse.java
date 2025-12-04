package com.lynn.museum.api.file.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 文件URL响应
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
public class FileUrlResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID
     */
    private Long fileId;

    /**
     * 文件访问URL
     */
    private String url;

    /**
     * 文件名
     */
    private String fileName;

    /**
     * 原始文件名
     */
    private String originalName;
}
