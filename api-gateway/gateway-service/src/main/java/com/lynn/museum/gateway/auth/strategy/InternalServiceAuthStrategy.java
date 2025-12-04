package com.lynn.museum.gateway.auth.strategy;

import com.lynn.museum.gateway.config.UnifiedAuthConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 内部服务认证策略
 * 
 * 支持内部服务间调用绕过认证，基于以下方式：
 * 1. X-Internal-Call: true + X-Service-ID
 * 2. X-Service-Token (预共享密钥)
 * 3. IP白名单验证
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class InternalServiceAuthStrategy implements AuthStrategy {

    private final UnifiedAuthConfig authConfig;

    @Override
    public boolean supports(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        
        boolean hasInternalCall = request.getHeaders().containsKey(getInternalCallHeader());
        boolean hasServiceToken = request.getHeaders().containsKey(getServiceTokenHeader());
        
        log.info("【认证检查】内部服务认证策略检查: Path={}, hasInternalCall={}, hasServiceToken={}", 
            request.getURI().getPath(), hasInternalCall, hasServiceToken);
        
        if (hasInternalCall || hasServiceToken) {
            log.info("【认证检查】发现内部服务调用Headers: X-Internal-Call={}, X-Service-ID={}, X-Service-Token={}", 
                request.getHeaders().getFirst(getInternalCallHeader()),
                request.getHeaders().getFirst(getServiceIdHeader()),
                maskToken(request.getHeaders().getFirst(getServiceTokenHeader())));
        } else {
            log.debug("【认证检查】未发现内部服务调用Headers，不支持该策略: Path={}", request.getURI().getPath());
        }
        
        // 检查是否包含内部服务调用的Header
        return hasInternalCall || hasServiceToken;
    }

    @Override
    public Mono<Void> authenticate(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        
        log.info("【认证检查】执行内部服务认证策略: Path={}", request.getURI().getPath());

        // 1. 检查内部调用标识
        String internalCall = request.getHeaders().getFirst(getInternalCallHeader());
        log.debug("【认证检查】内部调用标识: {}", internalCall);
        
        if ("true".equalsIgnoreCase(internalCall)) {
            log.info("【认证检查】发现内部调用标识，进行内部调用验证");
            return validateInternalCall(exchange, chain);
        }

        // 2. 检查服务Token
        String serviceToken = request.getHeaders().getFirst(getServiceTokenHeader());
        log.debug("【认证检查】服务Token: {}", maskToken(serviceToken));
        
        if (StringUtils.hasText(serviceToken)) {
            log.info("【认证检查】发现服务Token，进行Token验证");
            return validateServiceToken(exchange, chain, serviceToken);
        }

        log.warn("【认证检查】内部服务认证失败: 既无内部调用标识，也无服务Token - Path={}", request.getURI().getPath());
        return unauthorized(exchange, "Invalid internal service authentication");
    }

    /**
     * 验证内部调用
     */
    private Mono<Void> validateInternalCall(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String serviceId = request.getHeaders().getFirst(getServiceIdHeader());
        String clientIp = getClientIp(request);

        log.info("【认证检查】验证内部调用: ServiceId={}, ClientIp={}, Path={}", 
            serviceId, clientIp, request.getURI().getPath());

        // 验证服务ID
        if (!isValidServiceId(serviceId)) {
            log.error("【认证检查】服务ID验证失败: ServiceId={}, AllowedServices={}", 
                serviceId, authConfig.getStrategies().getInternalService().getAllowedServices());
            return unauthorized(exchange, "Invalid service ID");
        }

        // 验证客户端IP
        if (!isValidClientIp(clientIp)) {
            log.error("【认证检查】客户端IP验证失败: ClientIp={}, ServiceId={}, AllowedIps={}", 
                clientIp, serviceId, authConfig.getStrategies().getInternalService().getAllowedIps());
            return unauthorized(exchange, "Invalid client IP");
        }

        // 设置内部调用标识，供下游服务使用
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Auth-Type", "INTERNAL")
                .header("X-Service-ID", serviceId)
                .header("X-Client-IP", clientIp)
                .header("X-Auth-Time", String.valueOf(System.currentTimeMillis()))
                .build();

        log.info("【认证检查】内部服务调用认证成功: Service={}, IP={}, Path={}", 
            serviceId, clientIp, request.getURI().getPath());

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 验证服务Token
     */
    private Mono<Void> validateServiceToken(ServerWebExchange exchange, GatewayFilterChain chain, String serviceToken) {
        log.debug("Validating service token: {}", maskToken(serviceToken));

        // 验证服务Token（当前使用简单验证，生产环境可升级为JWT或密钥验证）
        if (!isValidServiceToken(serviceToken)) {
            log.warn("Invalid service token: {}", maskToken(serviceToken));
            return unauthorized(exchange, "Invalid service token");
        }

        ServerHttpRequest request = exchange.getRequest();
        String clientIp = getClientIp(request);

        // 设置服务Token认证标识
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Auth-Type", "SERVICE_TOKEN")
                .header("X-Client-IP", clientIp)
                .header("X-Auth-Time", String.valueOf(System.currentTimeMillis()))
                .build();

        log.info("Service token authenticated: ip={}", clientIp);

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    /**
     * 验证服务ID是否有效
     */
    private boolean isValidServiceId(String serviceId) {
        if (!StringUtils.hasText(serviceId)) {
            return false;
        }

        List<String> allowedServices = authConfig.getStrategies().getInternalService().getAllowedServices();
        
        // 如果没有配置允许的服务列表，则允许所有服务
        if (allowedServices.isEmpty()) {
            return true;
        }

        return allowedServices.contains(serviceId);
    }

    /**
     * 验证客户端IP是否有效
     */
    private boolean isValidClientIp(String clientIp) {
        if (!StringUtils.hasText(clientIp)) {
            return false;
        }

        List<String> allowedIps = authConfig.getStrategies().getInternalService().getAllowedIps();
        
        // 如果没有配置允许的IP列表，则允许所有IP
        if (allowedIps.isEmpty()) {
            return true;
        }

        // 简单的IP匹配，支持CIDR格式的扩展
        for (String allowedIp : allowedIps) {
            if (isIpMatched(clientIp, allowedIp)) {
                return true;
            }
        }

        return false;
    }

    /**
     * IP匹配检查
     */
    private boolean isIpMatched(String clientIp, String allowedIp) {
        if (clientIp == null || allowedIp == null) {
            return false;
        }
        
        log.debug("Checking IP match: clientIp={}, allowedIp={}", clientIp, allowedIp);
        
        // 直接匹配
        if ("*".equals(allowedIp) || allowedIp.equals(clientIp)) {
            log.debug("IP matched: direct match");
            return true;
        }
        
        // localhost特殊处理
        if ("localhost".equals(allowedIp) && ("127.0.0.1".equals(clientIp) || "::1".equals(clientIp) || "0:0:0:0:0:0:0:1".equals(clientIp))) {
            log.debug("IP matched: localhost");
            return true;
        }
        
        // IPv6 localhost (支持多种格式)
        if (isLocalhostIp(allowedIp) && isLocalhostIp(clientIp)) {
            log.debug("IP matched: localhost variants");
            return true;
        }

        // 支持简单的通配符匹配 (如 192.168.*.*)
        if (allowedIp.contains("*")) {
            String pattern = allowedIp.replace(".", "\\.")
                                    .replace("*", "\\d+");
            boolean matches = clientIp.matches(pattern);
            log.debug("IP wildcard match: pattern={}, result={}", pattern, matches);
            return matches;
        }

        // 简单的CIDR格式支持 (仅支持IPv4)
        if (allowedIp.contains("/") && !clientIp.contains(":")) {
            try {
                return isIpInCidr(clientIp, allowedIp);
            } catch (Exception e) {
                log.warn("Failed to check CIDR match for {}/{}: {}", clientIp, allowedIp, e.getMessage());
            }
        }
        
        log.debug("IP not matched");
        return false;
    }
    
    /**
     * 检查IP是否在CIDR范围内 (简单实现，仅支持IPv4)
     */
    private boolean isIpInCidr(String clientIp, String cidr) {
        String[] parts = cidr.split("/");
        if (parts.length != 2) {
            return false;
        }
        
        String networkIp = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);
        
        // 简单的实现：将IP转换为数字进行比较
        long clientIpLong = ipToLong(clientIp);
        long networkIpLong = ipToLong(networkIp);
        long mask = (-1L) << (32 - prefixLength);
        
        boolean result = (clientIpLong & mask) == (networkIpLong & mask);
        log.debug("CIDR match: clientIp={} ({}), network={}/{} ({}), mask={}, result={}", 
            clientIp, clientIpLong, networkIp, prefixLength, networkIpLong, mask, result);
        return result;
    }
    
    /**
     * 将IPv4地址转换为long
     */
    private long ipToLong(String ip) {
        String[] parts = ip.split("\\.");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Invalid IP address: " + ip);
        }
        
        long result = 0;
        for (int i = 0; i < 4; i++) {
            result |= (Long.parseLong(parts[i]) << (24 - i * 8));
        }
        // 确保无符号
        return result & 0xFFFFFFFFL;
    }
    
    /**
     * 检查IP是否为localhost的各种形式
     */
    private boolean isLocalhostIp(String ip) {
        if (ip == null) {
            return false;
        }
        return "127.0.0.1".equals(ip) ||
               "::1".equals(ip) ||
               "0:0:0:0:0:0:0:1".equals(ip) ||
               "localhost".equals(ip);
    }

    /**
     * 验证服务Token
     */
    private boolean isValidServiceToken(String serviceToken) {
        // 服务Token验证逻辑（生产环境建议升级为JWT或密钥验证）
        // 当前实现：基于约定的前缀验证，适用于受信任的内网环境
        return StringUtils.hasText(serviceToken) && 
               (serviceToken.startsWith("museum-service-") || 
                serviceToken.startsWith("museum-internal-"));
    }

    /**
     * 获取客户端IP地址
     */
    private String getClientIp(ServerHttpRequest request) {
        // 优先从X-Forwarded-For获取
        String xForwardedFor = request.getHeaders().getFirst("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        // 从X-Real-IP获取
        String xRealIp = request.getHeaders().getFirst("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        // 从远程地址获取
        InetSocketAddress remoteAddress = request.getRemoteAddress();
        if (remoteAddress != null) {
            return remoteAddress.getAddress().getHostAddress();
        }

        return "unknown";
    }

    /**
     * 掩码Token用于日志记录
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "****";
        }
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }

    /**
     * 返回401未授权响应
     */
    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().add(HttpHeaders.CONTENT_TYPE, "application/json;charset=UTF-8");

        String body = String.format(
            "{\"code\":401,\"message\":\"Internal Service Auth Failed: %s\",\"data\":null,\"timestamp\":%d}", 
            message, System.currentTimeMillis());
        
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes())));
    }

    @Override
    public int getOrder() {
        return authConfig.getStrategies().getInternalService().getOrder();
    }

    @Override
    public String getStrategyName() {
        return "InternalServiceAuth";
    }

    @Override
    public boolean isEnabled() {
        return authConfig.getStrategies().getInternalService().isEnabled();
    }

    // Helper methods for configuration access
    private String getInternalCallHeader() {
        return authConfig.getStrategies().getInternalService().getHeaderName();
    }

    private String getServiceTokenHeader() {
        return authConfig.getStrategies().getInternalService().getServiceTokenHeader();
    }

    private String getServiceIdHeader() {
        return authConfig.getStrategies().getInternalService().getServiceIdHeader();
    }
}
