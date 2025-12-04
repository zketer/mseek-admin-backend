#!/bin/bash

# Auth Service 停止脚本
# 版本: 1.0.0
# 作者: lynn

# 设置环境变量
export SERVICE_NAME="auth-service"

# 获取脚本所在目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
APP_HOME="$(dirname "$SCRIPT_DIR")"

# 设置路径
PID_FILE="$APP_HOME/logs/$SERVICE_NAME.pid"

# 检查PID文件是否存在
if [ ! -f "$PID_FILE" ]; then
    echo "$SERVICE_NAME 没有运行"
    exit 0
fi

# 读取PID
PID=$(cat "$PID_FILE")

# 检查进程是否存在
if ! ps -p $PID > /dev/null 2>&1; then
    echo "$SERVICE_NAME 进程不存在 (PID: $PID)"
    rm -f "$PID_FILE"
    exit 0
fi

echo "正在停止 $SERVICE_NAME (PID: $PID)..."

# 优雅停止
kill -TERM $PID

# 等待进程停止
for i in {1..30}; do
    if ! ps -p $PID > /dev/null 2>&1; then
        echo "$SERVICE_NAME 已停止"
        rm -f "$PID_FILE"
        exit 0
    fi
    sleep 1
done

# 强制停止
echo "优雅停止超时，强制停止..."
kill -KILL $PID

# 再次检查
sleep 2
if ps -p $PID > /dev/null 2>&1; then
    echo "错误: 无法停止 $SERVICE_NAME"
    exit 1
else
    echo "$SERVICE_NAME 已强制停止"
    rm -f "$PID_FILE"
fi
