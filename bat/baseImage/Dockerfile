from openjdk:8u312-slim-buster
COPY sources.list /etc/apt/sources.list
COPY i18n /usr/share/i18n
ADD apache-maven-3.9.6-bin.tar.gz /home/maven
RUN apt-get update && apt-get install curl mycli redis-tools procps telnet net-tools iputils-ping lrzsz vim locales wget expect -y && cp -f /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && rm -rf /var/lib/apt/lists/*
COPY settings.xml /root/.m2/
RUN ln -s /home/maven/apache-maven-3.9.6/bin/mvn /usr/local/bin/mvn
ENV LANG zh_CN.UTF-8
ENV LC_ALL zh_CN.UTF-8
ENV TZ Asia/Shanghai
RUN localedef -c -f UTF-8 -i zh_CN zh_CN.utf8
ENV MAVEN_HOME /home/maven/apache-maven-3.9.6/
ENV MAVEN_CONFIG /root/.m2
COPY --from=hengyunabc/arthas:latest /opt/arthas /opt/arthas
COPY jmx_prometheus_javaagent-0.20.0.jar /jmx-prometheus/
COPY config.yaml /jmx-prometheus/
