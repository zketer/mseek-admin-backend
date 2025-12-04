package com.lynn.museum.api.file.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 批量获取文件URL请求
 *
 * @author lynn
 * @since 2024-01-01
 */
@Data
public class BatchFileUrlRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 文件ID列表
     */
    private List<Long> fileIds;
}
