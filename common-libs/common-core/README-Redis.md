# Common-Core Redis 统一配置

## 📦 模块说明

`common-core` 模块提供了统一的 Redis 配置和工具类，所有微服务通过依赖引入即可使用。

## 🎯 设计目标

1. **代码复用**：避免各服务重复配置 Redis
2. **统一管理**：Redis 配置和工具类集中维护
3. **性能优化**：内置连接池优化、自动重连、重试机制
4. **开箱即用**：Spring Boot 自动配置，无需手动配置

## 📁 目录结构

```
common-core/
└── src/main/java/com/lynn/museum/common/redis/
    ├── config/
    │   └── RedisAutoConfiguration.java    # 自动配置类
    └── utils/
        └── RedisUtils.java                 # Redis 工具类
```

## ✨ 核心特性

### 1. 自动配置
- ✅ Spring Boot 自动装配
- ✅ Lettuce 客户端优化
- ✅ TCP KeepAlive
- ✅ 自动重连
- ✅ 连接池健康检查

### 2. 重试机制
- 最多重试 3 次
- 指数退避策略（100ms → 200ms → 300ms）
- 自动捕获超时和连接失败异常

### 3. 丰富的工具方法
- String 操作：`get()`, `set()`, `incr()`, `decr()`
- Hash 操作：`hget()`, `hset()`, `hmget()`, `hmset()`
- Set 操作：`sGet()`, `sSet()`, `sHasKey()`
- List 操作：`lGet()`, `lSet()`, `lRemove()`
- 分布式锁：`tryLock()`, `releaseLock()`

## 🚀 使用方式

### 1. 引入依赖

各服务的 `pom.xml` 中已经引入了 `common-core`：

```xml
<dependency>
    <groupId>com.lynn</groupId>
    <artifactId>common-core</artifactId>
    <version>0.0.1</version>
</dependency>
```

### 2. 配置 Redis

在 `application.yml` 中配置 Redis 连接信息：

```yaml
spring:
  data:
    redis:
      host: 123.56.12.253
      port: 6379
      password: h2vMDLpFgeTCs2n8
      database: 0
      timeout: 10000ms
      lettuce:
        pool:
          max-active: 8
          max-wait: -1ms
          max-idle: 8
          min-idle: 2
          time-between-eviction-runs: 60000
        shutdown-timeout: 100ms
      client-type: lettuce
      connect-timeout: 5000ms
      test-on-borrow: true
      test-on-return: false
      test-while-idle: true
```

### 3. 使用 RedisUtils

```java
@Service
public class YourService {
    
    @Autowired
    private RedisUtils redisUtils;
    
    public void example() {
        // String 操作
        redisUtils.set("key", "value", 3600);
        String value = (String) redisUtils.get("key");
        
        // Hash 操作
        redisUtils.hset("user:1", "name", "张三");
        Object name = redisUtils.hget("user:1", "name");
        
        // 分布式锁
        if (redisUtils.tryLock("lock:order:123", "uuid", 30)) {
            try {
                // 业务逻辑
            } finally {
                redisUtils.releaseLock("lock:order:123", "uuid");
            }
        }
    }
}
```

## 🔧 配置说明

### Lettuce 客户端优化

自动配置了以下优化：

| 配置项 | 值 | 说明 |
|--------|---|------|
| 连接超时 | 5秒 | 建立连接的超时时间 |
| 命令超时 | 10秒 | Redis 命令执行超时 |
| TCP KeepAlive | 启用 | 保持连接活跃 |
| 自动重连 | 启用 | 连接断开自动重连 |
| 连接验证 | 启用 | 获取连接时验证有效性 |

### 连接池配置

| 配置项 | 推荐值 | 说明 |
|--------|-------|------|
| max-active | 8 | 最大活跃连接数 |
| max-idle | 8 | 最大空闲连接数 |
| min-idle | 2 | 最小空闲连接数 |
| time-between-eviction-runs | 60000 | 空闲连接检测周期（毫秒） |

## 📊 性能优化

### 1. 连接池优化
- 保持最小空闲连接，避免冷启动
- 定期检测并清理无效连接
- 获取连接时验证有效性

### 2. 重试机制
- 自动重试超时和连接失败的操作
- 指数退避避免雪崩
- 详细的日志记录

### 3. 长连接保持
- TCP KeepAlive 保持连接活跃
- 自动重连机制
- 连接断开时优雅处理

## 🐛 故障排查

### 问题：服务启动报错找不到 RedisUtils

**原因**：`common-core` 未正确引入或版本不匹配

**解决**：
```bash
# 重新编译 common-core
cd common-libs/common-core
mvn clean install

# 重新编译服务
cd ../../auth-center/auth-service
mvn clean package
```

### 问题：Redis 连接超时

**原因**：Redis 服务器不可达或配置错误

**解决**：
1. 检查 Redis 服务器是否运行
2. 检查网络连接和防火墙
3. 验证 `application.yml` 中的配置

### 问题：长时间空闲后连接失效

**原因**：连接池配置不当

**解决**：
- 确保 `min-idle: 2`（保持最小空闲连接）
- 确保 `test-while-idle: true`（空闲时测试连接）
- 确保 `time-between-eviction-runs: 60000`（定期检测）

## 📝 更新日志

### v0.0.1 (2024-11-18)
- ✨ 初始版本
- ✅ 统一 Redis 配置
- ✅ 提供 RedisUtils 工具类
- ✅ 支持自动配置
- ✅ 内置重试机制
- ✅ Lettuce 客户端优化

## 👥 维护者

- lynn (@zketer)

## 📄 许可证

MIT License
