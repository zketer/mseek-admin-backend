package com.lynn.museum.auth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lynn.museum.auth.model.entity.OAuth2UserProvider;
import org.apache.ibatis.annotations.Mapper;

/**
 * OAuth2第三方身份提供商绑定Mapper
 * 使用MyBatis Plus，所有查询方法通过LambdaQueryWrapper实现
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Mapper
public interface OAuth2UserProviderMapper extends BaseMapper<OAuth2UserProvider> {
    // 所有查询方法都通过Service层使用LambdaQueryWrapper实现
    // 不再需要自定义方法和XML映射文件
}
