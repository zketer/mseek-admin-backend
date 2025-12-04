#!/bin/bash

# gateway-service 启动脚本
# 版本: 1.0.0
# 作者: lynn

# 设置环境变量
export JAVA_HOME=${JAVA_HOME:-/usr/lib/jvm/java-17-openjdk}
export SERVICE_NAME="gateway-service"
export SERVICE_VERSION="0.0.1"
export SPRING_PROFILES_ACTIVE=${SPRING_PROFILES_ACTIVE:-prod}

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_HOME="$(dirname "$SCRIPT_DIR")"

# 设置路径
JAR_FILE="$APP_HOME/lib/$SERVICE_NAME-$SERVICE_VERSION.jar"
CONFIG_DIR="$APP_HOME/config"
LOG_DIR="$APP_HOME/logs"
PID_FILE="$APP_HOME/logs/$SERVICE_NAME.pid"

# 创建日志目录
mkdir -p "$LOG_DIR"

# 检查JAR文件是否存在
if [ ! -f "$JAR_FILE" ]; then
    echo "错误: JAR文件不存在: $JAR_FILE"
    exit 1
fi

# Docker环境下不需要检查PID文件
# 清理可能存在的PID文件
rm -f "$PID_FILE"

# JVM参数配置
JVM_OPTS="-server"
JVM_OPTS="$JVM_OPTS -Xms512m -Xmx1024m"
JVM_OPTS="$JVM_OPTS -XX:+UseG1GC"
JVM_OPTS="$JVM_OPTS -XX:MaxGCPauseMillis=200"
JVM_OPTS="$JVM_OPTS -XX:+UnlockExperimentalVMOptions"
JVM_OPTS="$JVM_OPTS -XX:+UseContainerSupport"
JVM_OPTS="$JVM_OPTS -Djava.security.egd=file:/dev/./urandom"
JVM_OPTS="$JVM_OPTS -Dspring.profiles.active=$SPRING_PROFILES_ACTIVE"
JVM_OPTS="$JVM_OPTS -Dspring.config.location=classpath:/application.yml,file:$CONFIG_DIR/application-$SPRING_PROFILES_ACTIVE.yml,file:$CONFIG_DIR/application-unified-auth.yml"

echo "正在启动 $SERVICE_NAME..."
echo "JAR文件: $JAR_FILE"
echo "配置目录: $CONFIG_DIR"
echo "日志目录: $LOG_DIR"
echo "JVM参数: $JVM_OPTS"

# 启动服务（作为Docker主进程，前台运行）
echo "启动命令: $JAVA_HOME/bin/java $JVM_OPTS -jar $JAR_FILE"
echo "=========================================="
echo "日志将同时输出到控制台和文件: $LOG_DIR/startup.log"

# 执行Java应用，同时输出到stdout和日志文件
exec $JAVA_HOME/bin/java $JVM_OPTS -jar "$JAR_FILE" 2>&1 | tee "$LOG_DIR/startup.log"
