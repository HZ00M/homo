# 使用 debian:buster-slim 作为基础镜像
FROM default.registry.tke-syyx.com/om/debian:buster-slim

# 设置环境变量
ENV PYTHON_VERSION=3.10.0

# 替换 Debian 镜像源为阿里云源
COPY sources.list /etc/apt/sources.list

# 安装构建 Python 所需的依赖
RUN apt-get update && apt-get install -y \
    wget \
    build-essential \
    libssl-dev \
    libbz2-dev \
    libreadline-dev \
    libsqlite3-dev \
    libffi-dev \
    zlib1g-dev\
    libpq-dev \
    python3-dev \
    gcc \
    make \
    xz-utils \
    liblzma-dev \
    nodejs \
    npm \
    && rm -rf /var/lib/apt/lists/*

# 下载并解压 Python 源代码
RUN wget https://www.python.org/ftp/python/${PYTHON_VERSION}/Python-${PYTHON_VERSION}.tgz \
    && tar -xzf Python-${PYTHON_VERSION}.tgz \
    && cd Python-${PYTHON_VERSION} \
    && ./configure --enable-optimizations \
    && make altinstall \
    && cd .. \
    && rm -rf Python-${PYTHON_VERSION} Python-${PYTHON_VERSION}.tgz



# 设置默认命令
CMD ["python${PYTHON_VERSION}", "--version"]
