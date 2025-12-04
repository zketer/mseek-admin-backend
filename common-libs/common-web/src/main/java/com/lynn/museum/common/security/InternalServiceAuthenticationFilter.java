package com.lynn.museum.common.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * 内部服务调用认证过滤器
 * 
 * 通用的微服务内部调用认证策略：
 * 1. 检查请求Header中是否包含内部服务调用标识
 * 2. 如果是内部调用，自动通过Spring Security认证
 * 3. 内部服务调用拥有超级权限，绕过所有业务权限检查
 * 4. 避免硬编码URL路径，实现真正的通用性
 * 
 * 适用场景：
 * - 所有微服务都可以使用此过滤器
 * - 无需在SecurityConfig中硬编码具体路径
 * - 支持动态添加内部服务接口
 * - 内部服务调用无需业务权限验证
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
public class InternalServiceAuthenticationFilter extends OncePerRequestFilter {

    private static final String INTERNAL_CALL_HEADER = "X-Internal-Call";
    private static final String SERVICE_ID_HEADER = "X-Service-ID";
    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";
    
    // 内部服务角色权限
    private static final String INTERNAL_SERVICE_AUTHORITY = "ROLE_INTERNAL_SERVICE";

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String requestURI = request.getRequestURI();
        log.info("【认证检查】内部服务认证过滤器执行: URI={}", requestURI);
        
        // 检查是否为内部服务调用
        if (isInternalServiceCall(request)) {
            String serviceId = request.getHeader(SERVICE_ID_HEADER);
            log.info("【认证检查】检测到内部服务调用，自动认证通过: URI={}, ServiceId={}", requestURI, serviceId);
            
            // 创建内部服务认证token，保持简单
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                "INTERNAL_SERVICE:" + serviceId,
                null,
                Collections.singletonList(new SimpleGrantedAuthority(INTERNAL_SERVICE_AUTHORITY))
            );
            
            SecurityContextHolder.getContext().setAuthentication(authToken);
            log.info("【认证检查】内部服务认证上下文已设置: ServiceId={}, Principal={}, Authorities={}", 
                serviceId, 
                SecurityContextHolder.getContext().getAuthentication().getName(),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
        } else {
            log.debug("【认证检查】非内部服务调用，继续正常认证流程: URI={}", requestURI);
        }
        
        filterChain.doFilter(request, response);
    }
    
    /**
     * 判断是否为内部服务调用
     */
    private boolean isInternalServiceCall(HttpServletRequest request) {
        String internalCall = request.getHeader(INTERNAL_CALL_HEADER);
        String serviceId = request.getHeader(SERVICE_ID_HEADER);
        String serviceToken = request.getHeader(SERVICE_TOKEN_HEADER);
        
        boolean isInternal = "true".equalsIgnoreCase(internalCall) && 
                            StringUtils.hasText(serviceId) && 
                            StringUtils.hasText(serviceToken);
        
        if (isInternal) {
            log.debug("【认证检查】内部服务调用验证: InternalCall={}, ServiceId={}, HasToken={}", 
                internalCall, serviceId, StringUtils.hasText(serviceToken));
        }
        
        return isInternal;
    }
}
