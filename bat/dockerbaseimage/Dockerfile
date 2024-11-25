# 基于 Docker 官方的 dind 镜像
FROM default.registry.tke-syyx.com/syyx-tpf/docker:20.10-dind

# 设置时区环境变量（可选，根据需要修改）
ENV TZ=Asia/Shanghai

# 安装其他必要的包（如需要）
RUN apk update && \
    apk add --no-cache bash && \
    apk add --no-cache openrc  # 安装 openrc 以支持启动管理

# 默认命令，启动 Docker 服务
CMD ["dockerd-entrypoint.sh"]
