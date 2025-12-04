package com.lynn.museum.common.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Flyway 数据库版本管理通用配置
 * 所有服务都可以使用此配置进行数据库版本管理
 * 
 * @author lynn
 * @since 2024-01-01
 */
@Configuration
public class FlywayConfig {

    /**
     * 开发环境下的Flyway迁移策略
     * 允许清理数据库并重新迁移（仅开发环境）
     */
    @Bean
    @Profile("dev")
    public FlywayMigrationStrategy cleanMigrateStrategy() {
        return flyway -> {
            // 开发环境下可以清理数据库（谨慎使用）
            // flyway.clean();
            flyway.migrate();
        };
    }

    /**
     * 生产环境下的Flyway迁移策略
     * 只允许迁移，不允许清理
     */
    @Bean
    @Profile({"prod", "test", "staging"})
    public FlywayMigrationStrategy safeMigrateStrategy() {
        return Flyway::migrate;
    }
    
    /**
     * 默认迁移策略（当没有指定profile时）
     * 采用安全策略，只允许迁移
     */
    @Bean
    @Profile("default")
    public FlywayMigrationStrategy defaultMigrateStrategy() {
        return Flyway::migrate;
    }
}