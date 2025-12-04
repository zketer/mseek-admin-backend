package com.lynn.museum.file.enums;

import lombok.Getter;

/**
 * 文件类型枚举
 * 
 * @author Lynn
 * @since 2024-01-01
 */
@Getter
public enum FileTypeEnum {

    /**
     * 头像
     */
    AVATAR("avatar", "头像"),

    /**
     * 博物馆图片
     */
    MUSEUM("museum", "博物馆图片"),

    /**
     * 公告图片
     */
    ANNOUNCEMENT("announcement", "公告图片"),

    /**
     * 横幅图片
     */
    BANNER("banner", "横幅图片"),

    /**
     * 推荐内容图片
     */
    RECOMMENDATION("recommendation", "推荐内容图片"),

    /**
     * 用户打卡照片
     */
    CHECKIN("checkin", "用户打卡照片"),

    /**
     * 展览图片
     */
    EXHIBITION("exhibition", "展览图片"),

    /**
     * 应用版本安装包
     */
    APP_VERSION("app-version", "应用版本安装包"),

    /**
     * 临时文件
     */
    TEMP("temp", "临时文件");

    /**
     * 类型代码
     * -- GETTER --
     *  获取代码

     */
    private final String code;

    /**
     * 类型描述
     * -- GETTER --
     *  获取描述

     */
    private final String description;

    /**
     * 构造器
     */
    FileTypeEnum(String code, String description) {
        this.code = code;
        this.description = description;
    }

    /**
     * 根据代码获取枚举
     */
    public static FileTypeEnum fromCode(String code) {
        for (FileTypeEnum type : values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("未知的文件类型: " + code);
    }
}
