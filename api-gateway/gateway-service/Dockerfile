# 使用多阶段构建优化镜像大小
FROM maven:3.8-openjdk-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制整个项目（简化构建）
COPY . .

# 设置Maven选项
ENV MAVEN_OPTS="-Dmaven.artifact.threads=5"

# 构建应用
RUN mvn clean package -pl gateway-service -am -DskipTests

# 运行阶段
FROM eclipse-temurin:17-jre

# 安装必要工具
RUN apt-get update && apt-get install -y \
    curl \
    && rm -rf /var/lib/apt/lists/*

# 创建应用用户
RUN groupadd -r museum && useradd -r -g museum museum

# 设置工作目录
WORKDIR /app

# 复制JAR包
COPY --from=builder /app/gateway-service/target/gateway-service-*.jar app.jar

# 创建日志目录
RUN mkdir -p /app/logs && chown -R museum:museum /app

# 切换到应用用户
USER museum

# 健康检查
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8000/actuator/health || exit 1

# 暴露端口
EXPOSE 8000

# JVM优化参数
ENV JAVA_OPTS="-Xms512m -Xmx1g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/app/logs/"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
