package com.lynn.museum.file.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
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

/**
 * Swagger配置类
 *
 * @author lynn
 * @since 2024-01-01
 */
@Configuration
@EnableConfigurationProperties(SwaggerConfig.SwaggerServerProperties.class)
@RequiredArgsConstructor
@Slf4j
public class SwaggerConfig {

    private final SwaggerServerProperties swaggerServerProperties;

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.address:localhost}")
    private String serverAddress;

    @Bean
    public OpenAPI museumInfoOpenAPI() {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("文件服务API文档")
                        .version("v0.0.1")
                        .contact(new Contact()
                                .name("Lynn")
                                .email("lynn@example.com"))
                        .license(new License().name("Apache 2.0").url("http://springdoc.org")))
                .externalDocs(new ExternalDocumentation()
                        .description("文件服务系统文档")
                        .url("https://github.com/lynn/museums"));

//        // 如果配置了servers，则添加到OpenAPI中
//        if (swaggerServerProperties.getServers() != null && !swaggerServerProperties.getServers().isEmpty()) {
//            List<Server> servers = swaggerServerProperties.getServers().stream()
//                    .map(serverConfig -> new Server()
//                            .url(serverConfig.getUrl())
//                            .description(serverConfig.getDescription()))
//                    .collect(Collectors.toList());
//            openAPI.servers(servers);
//        } else {
//            // 默认添加本地服务地址
//            String serverUrl = String.format("http://%s:%s%s", serverAddress, serverPort,
//                    StringUtils.hasText(contextPath) ? contextPath : "");
//            openAPI.addServersItem(new Server()
//                    .url(serverUrl)
//                    .description("本地服务地址"));
//        }

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
