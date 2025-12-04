package com.lynn.museum.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * 微信小程序code2Session接口响应DTO
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Data
public class WechatCode2SessionResponse {

    /**
     * 用户唯一标识
     */
    @JsonProperty("openid")
    private String openid;

    /**
     * 会话密钥
     */
    @JsonProperty("session_key")
    private String sessionKey;

    /**
     * 用户在开放平台的唯一标识符，若当前小程序已绑定到微信开放平台账号下会返回
     */
    @JsonProperty("unionid")
    private String unionid;

    /**
     * 错误码
     */
    @JsonProperty("errcode")
    private Integer errcode;

    /**
     * 错误信息
     */
    @JsonProperty("errmsg")
    private String errmsg;

    /**
     * 是否成功
     */
    public boolean isSuccess() {
        return errcode == null || errcode == 0;
    }
}
