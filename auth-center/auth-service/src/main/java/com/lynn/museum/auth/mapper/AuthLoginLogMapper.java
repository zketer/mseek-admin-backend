package com.lynn.museum.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.auth.model.entity.AuthLoginLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 认证登录日志Mapper
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface AuthLoginLogMapper extends BaseMapper<AuthLoginLog> {
    
    /**
     * 统计用户成功登录次数
     * 
     * @param userId 用户ID
     * @return 登录次数
     */
    @Select("SELECT COUNT(*) FROM auth_login_log WHERE user_id = #{userId} AND login_result = 1")
    Integer countSuccessLoginsByUserId(@Param("userId") Long userId);
    
    /**
     * 获取用户最后一次成功登录记录
     * 
     * @param userId 用户ID
     * @return 最后登录记录
     */
    @Select("SELECT * FROM auth_login_log WHERE user_id = #{userId} AND login_result = 1 ORDER BY create_at DESC LIMIT 1")
    AuthLoginLog getLastSuccessLoginByUserId(@Param("userId") Long userId);
}
