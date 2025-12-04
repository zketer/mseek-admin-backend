package com.lynn.museum.auth.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Swagger配置类
 */
@Configuration
@EnableConfigurationProperties(SwaggerConfig.SwaggerServerProperties.class)
@RequiredArgsConstructor
@Slf4j
public class SwaggerConfig {

    private final SwaggerServerProperties swaggerServerProperties;
    
    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Bean
    public OpenAPI customOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("博物馆认证服务API")
                        .version("1.0")
                        .description("博物馆系统认证服务API文档")
                        .license(new License().name("Apache 2.0")
                                .url("http://www.apache.org/licenses/LICENSE-2.0.html")))
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
                .components(new Components()
                        .addSecuritySchemes("bearerAuth", new SecurityScheme()
                                .name("Authorization")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));

        // 如果配置了servers，则添加到OpenAPI中
        if (swaggerServerProperties.getServers() != null && !swaggerServerProperties.getServers().isEmpty()) {
            List<Server> servers = swaggerServerProperties.getServers().stream()
                    .map(serverConfig -> new Server()
                            .url(serverConfig.getUrl())
                            .description(serverConfig.getDescription()))
                    .collect(Collectors.toList());
            openAPI.servers(servers);
        }

        return openAPI;
    }

    @Bean
    public OpenApiCustomizer addContextPath() {
        return openApi -> {
            if (StringUtils.hasText(contextPath) && !"/".equals(contextPath)) {
                log.info("Adding context-path '{}' to OpenAPI paths", contextPath);
                Paths paths = openApi.getPaths();
                Paths updatedPaths = new Paths();

                paths.forEach((path, pathItem) -> {
                    // 为路径添加 context-path 前缀
                    String updatedPath = contextPath + path;
                    updatedPaths.addPathItem(updatedPath, pathItem);
                });

                openApi.setPaths(updatedPaths);
            }
        };
    }

    /**
     * Swagger服务器配置属性
     */
    @ConfigurationProperties(prefix = "springdoc")
    public static class SwaggerServerProperties {
        private List<ServerConfig> servers;

        public List<ServerConfig> getServers() {
            return servers;
        }

        public void setServers(List<ServerConfig> servers) {
            this.servers = servers;
        }

        public static class ServerConfig {
            private String url;
            private String description;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getDescription() {
                return description;
            }

            public void setDescription(String description) {
                this.description = description;
            }
        }
    }
}