package com.lynn.museum.info.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 文件关系类型枚举
 * 用于标识文件在业务实体中的作用类型
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum RelationTypeEnum {
    
    /**
     * 主图/封面图
     */
    MAIN_IMAGE("MAIN_IMAGE", "主图"),
    
    /**
     * 缩略图
     */
    THUMBNAIL("THUMBNAIL", "缩略图"),
    
    /**
     * 图片库/相册
     */
    GALLERY("GALLERY", "图库"),
    
    /**
     * 详情图片
     */
    DETAIL_IMAGE("DETAIL_IMAGE", "详情图"),
    
    /**
     * 文档附件
     */
    DOCUMENT("DOCUMENT", "文档"),
    
    /**
     * 用户头像
     */
    AVATAR("AVATAR", "头像"),
    
    /**
     * 背景图片
     */
    BACKGROUND("BACKGROUND", "背景图"),
    
    /**
     * 二维码
     */
    QR_CODE("QR_CODE", "二维码"),
    
    /**
     * 视频文件
     */
    VIDEO("VIDEO", "视频"),
    
    /**
     * 音频文件
     */
    AUDIO("AUDIO", "音频"),
    
    /**
     * 安装包文件（APK/IPA）
     */
    INSTALLATION_PACKAGE("INSTALLATION_PACKAGE", "安装包"),
    
    /**
     * Logo图标
     */
    LOGO("LOGO", "Logo");

    /**
     * 枚举代码（存储到数据库）
     */
    @EnumValue
    @JsonValue
    private final String code;
    
    /**
     * 枚举描述
     */
    private final String description;

    /**
     * 根据代码获取枚举
     * 
     * @param code 枚举代码
     * @return 对应的枚举，未找到返回null
     */
    public static RelationTypeEnum fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        for (RelationTypeEnum type : RelationTypeEnum.values()) {
            if (type.getCode().equalsIgnoreCase(code.trim())) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * 检查代码是否有效
     * 
     * @param code 枚举代码
     * @return 是否有效
     */
    public static boolean isValidCode(String code) {
        return fromCode(code) != null;
    }
}
