package com.homo.core.maven;

public interface ConfigKey {
    String SCAN_CLASS_PATH_KEY = "server.scan.path";//插件基础配置
    String SCAN_CLASS_PATH_DEFAULT_VALUE = "com.homo";//插件基础配置
    String SCAN_SCOPE_KEY = "server.scan.scope";
    String DEFAULT_SCAN_SCOPE_DEFAULT_VALUE = "compile";
    String PROPERTY_FROM_KEY = "property.from";//资源加载来源 PROPERTY APOLLO
    String LOCAL_IP ="127.0.0.1";
    String EMPTY_CONFIG_VALUE = "EMPTY_VALUE";//表示空值
    String BOOLEAN_FALSE = "false";
    String CUSTOM_BUILD_NS_VALUE = "homo_build_config";//从哪个namespace读取构建配置
    String DEPLOY_DNS_UPDATE_ENABLE_KEY = "deploy.dns.config.enable";
    String DEPLOY_FILESYSTEM_PVC_ENABLE_KEY = "deploy.filesystem.pvc.enable";
    String LOCAL_DEBUG_KEY = "local.debug";
    String LOCAL_IP_KEY = "local.ip";
    String KUBE_SYSTEM_NAMESPACE = "kube-system";
    String APOLLO_APP_ID = "appId";
    String APOLLO_CLUSTER = "cluster";
    String APOLLO_NAMESPACE = "namespace";
    String APOLLO_NAMESPACE_PUBLIC = "public";
    /**
     * 工程根路径
     */
    String PROJECT_BASE_DIR = "PROJECT_BASE_DIR";
    String APOLLO_SERVER_PROPERTIES_PATH = "devops/apollo/server.properties";
    String PROJECT_GIT_IGNORE_DEVOPS_DIR_NAME = "devops";
    /**
     * dockerfile 文件 占位符
     */
    String DOCKER_EXAMPLE_COPY_KEY = "EXAMPLE-COPY";
    String DOCKER_EXAMPLE_COPY_TEMP = "COPY target/%s.jar /tmp/app.jar";
    String DOCKER_TARGET_FILE_PATH = "DockerFile";
    /**
     * 镜像配置
     */
    String DOCKER_REPO_ADDR_KEY = "docker.registry.addr";
    String DOCKER_REPO_USERNAME_KEY = "docker.registry.username";
    String DOCKER_REPO_PASSWORD_KEY = "docker.registry.password";
    String DOCKER_REPO_DIR_KEY = "docker.registry.dir";
    String DOCKER_BUILD_SUFFIX_KEY = "docker.image.suffix";
    String DOCKER_DEPLOY_ADDR_KEY = "docker.deploy.addr";
    String DOCKER_DEPLOY_DIR_KEY = "docker.deploy.dir";
    String DOCKER_PUSH_KEY = "docker.force.push";
    String DEPLOY_DOCKER_FILE_KEY = "docker.dockerfile.path";
    /**
     * apollo 配置文件
     */
    String APOLLO_FILE_KEY = "apollo.file";
    String APOLLO_META_KEY = "apollo.meta";
    String APOLLO_ENV_KEY = "apollo.env";
    String APOLLO_IDC_KEY = "apollo.idc";
    String APOLLO_ADDR_KEY = "apollo.addr";
    String APOLLO_TOKEN_KEY = "apollo.token";
    String APOLLO_EDITOR_KEY = "apollo.editor";
    String APOLLO_EDITOR_DEFAULT_VALUE = "homoEditor";
    String APOLLO_PROPERTY_FORMAT_VALUE = "properties";
    String DEPLOY_APOLLO_UPDATE_ENABLE_KEY = "apollo.update.enable";
    String APOLLO_UPDATE_STRATEGY_KEY = "apollo.update.strategy";
    String APOLLO_UPDATE_PATH = "apollo.update.path";
    String APOLLO_UPDATE_PATH_DEFAULT_VALUE = "devops/apollo";
    String APOLLO_UPDATE_STRATEGY_VALUE_SET_ABSENT = "SET_ABSENT";
    String APOLLO_UPDATE_STRATEGY_VALUE_SET = "SET";
    /**
     * k8s 配置文件
     */
    String K8S_CONFIG_FILE_KEY = "k8s.file";
    String K8S_CERT_CONFIG_KEY = "kube.cert.config";
    String K8S_NAMESPACE_KEY = "k8s.namespace";
    String K8S_CERT_CONFIG_FILE_KEY = "kube.cert.config.file";
    String K8S_CONFIG_FILE_DEFAULT_VALUE = "devops/k8s/kubeconfig.yaml";
    String DEPLOYMENT_TEMP_YAML = "devops/k8s/deployment_temp.yaml";
    String DEPLOYMENT_BUILD_YAML = "devops/k8s/deployment_build.yaml";
    String STATEFUL_SET_TEMP_YAML = "devops/k8s/statefulSet_temp.yaml";
    String STATEFUL_SET_BUILD_YAML = "devops/k8s/statefulSet_build.yaml";
    String HEADLESS_SERVICE_TEMP_YAML = "devops/k8s/headlessService_temp.yaml";
    String CLOUD_SERVICE_BUILD_YAML = "devops/k8s/cloud_service_build.yaml";
    String CLOUD_SERVICE_YAML = "devops/k8s/cloud_service.yaml";
    String CLUSTER_SERVICE_TEMP_YAML = "devops/k8s/clusterService_temp.yaml";
    String LOCAL_SERVICE_BUILD_YAML = "devops/k8s/local_service_build.yaml";
    String LOCAL_SERVICE_YAML = "devops/k8s/local_service.yaml";
    String ENDPOINTS_TEMP_YAML = "devops/k8s/endpoints_temp.yaml";
    String PVC_YAML = "devops/k8s/filesystem_pvc.yaml";
    String PVC_TEMP_YAML = "devops/k8s/filesystem_pvc_temp.yaml";
    String DEPLOY_DOCKER_FILE_TEMP = "devops/docker/Dockerfile_temp";
    String DEPLOY_DEPLOYMENT_FILE = "devops/k8s/deployment.yaml";
    String DEPLOY_STATEFUL_FILE = "devops/k8s/statefulSet.yaml";
    /**
     * 构建过程常量
     */
    String APP = "app";
    String CONTAINER = "container";
    String DEPLOYMENT = "deployment";
    String STATEFUL_LABELS = "isStateful";
    String ENV_GROUP = "HOMO_GROUP";
    String ENV_ARTIFACT = "HOMO_ARTIFACT";
    String ENV_VERSION = "HOMO_VERSION";
    String FILE_SYSTEM_PVC_SUFFIX = "-file-system-pvc";
    String PVC_SUB_DIR_LABEL = "dir";
    String DOMAIN_SUFFIX = ".svc.cluster.local";
    String DEFAULT_POD_NUM = "1";
    String CONTAINER_PARAM_REQUEST_CPU_DEFAULT_VALUE = "0";
    String CONTAINER_PARAM_LIMIT_CPU_DEFAULT_VALUE = "1";
    String CONTAINER_PARAM_REQUEST_MEMORY_DEFAULT_VALUE = "0";
    String CONTAINER_PARAM_LIMIT_MEMORY_DEFAULT_VALUE = "2Gi";
    String CONTAINER_PARAM_JAVA_OPTIONS_KEY = "param.java.options";
    String CONTAINER_PARAM_JAVA_OPTIONS_DEFAULT_VALUE = "-Xmx600m -Xms600m -XX:+UseG1GC";
    String CONTAINER_PARAM_LIMIT_CPU_KEY = "param.cpu.limit";
    String CONTAINER_PARAM_LIMIT_MEMORY_KEY = "param.mem.limit";
    String CONTAINER_PARAM_REQUEST_CPU_KEY = "param.cpu.request";
    String CONTAINER_PARAM_REQUEST_MEMORY_KEY = "param.mem.request";
    /**
     * 链路追踪
     */
    String SW_TRACING_ENABLE_KEY = "sky.walking.enable";
    String SW_ENV_BACKEND_SERVICE_KEY = "SW_AGENT_COLLECTOR_BACKEND_SERVICES";
    String SW_ENV_BACKEND_NAME_VALUE = "skywalking-oap.skywalking:11800";
    String SW_ENV_LOG_DIR_KEY = "SW_LOGGING_DIR";
    String SW_ENV_LOG_DIR_VALUE = "/tmp";
    String SW_ENV_AGENT_NAME_KEY = "SW_AGENT_NAME";
    String SW_PARAM_JAVA_OPTIONS = "-javaagent:/tmp/skywalking/skywalking-agent.jar";
    String SW_CONFIG_MAP_NAME = "skywalking-config";
    String SW_PVC_VOLUME_NAME = "volume-skyalking-tools";
    String SW_CONFIG_MAP_VOLUME_NAME = "volume-skyalking";
    String SW_CONFIG_MAP_VOLUME_MOUNT = "/opt/skywalking/config/";
    /**
     * 磁盘挂载
     */
    String K8S_FILESYSTEM_VOLUME_ENABLE_KEY = "k8s.filesystem.enable";
    String K8S_FILESYSTEM_PVC_SUFFIX= "-file-system-pvc";
    String K8S_FILESYSTEM_VOLUME_NAME_KEY = "volume.filesystem";
    String K8S_FILESYSTEM_VOLUME_NAME_DEFAULT_VALUE = "volume-filesystem";
    String K8S_FILESYSTEM_VOLUME_MOUNT= "/mnt/filesystem";
    /**
     * 健康检查
     */
    String READINESS_PROBE_CHECK_ENABLE_KEY = "readiness.probe.check.enable";
    String READINESS_PROBE_TYPE_KEY = "readiness.probe.check.type";
    String READINESS_PROBE_TYPE_HTTP = "http";
    String READINESS_PROBE_TYPE_TCP = "tcp";
    String READINESS_PROBE_TYPE_CMD = "cmd";
    String READINESS_PROBE_CMD_DEFAULT_VALUE = "cat /tmp/health";
    String READINESS_PROBE_TYPE_TCP_PORT_KEY = "readiness.probe.check.tcp.port";
    String READINESS_PROBE_TYPE_TCP_PORT_DEFAULT_VALUE = "80";
    String READINESS_PROBE_TYPE_HTTP_PATH_KEY = "readiness.probe.check.http.path";
    String READINESS_PROBE_TYPE_HTTP_PATH_DEFAULT_VALUE = "/health";
    String READINESS_PROBE_TYPE_HTTP_PORT_KEY = "readiness.probe.check.http.port";
    String READINESS_PROBE_TYPE_HTTP_PORT_DEFAULT_VALUE = "8080";
    String READINESS_PROBE_CMD_KEY = "readiness.probe.check.cmd";

    String SPEC_POD_NUM = "pod.num";
    String DEPLOYMENT_TEMP_YAML_KEY = "deployment.temp.yaml";
    String DEPLOYMENT_BUILD_YAML_KEY = "deployment.build.yaml";
    String STATEFUL_SET_TEMP_YAML_KEY = "statefulSet.temp.yaml";
    String STATEFUL_SET_BUILD_YAML_KEY = "statefulSet.build.yaml";
    String HEADLESS_SERVICE_TEMP_YAML_KEY = "headlessService.temp.yaml";
    String CLUSTER_SERVICE_TEMP_YAML_KEY = "clusterService.temp.yaml";
    String ENDPOINTS_TEMP_YAML_KEY = "endpoints.temp.yaml";
    String CLOUD_SERVICE_BUILD_YAML_KEY = "cloudService.build.yaml";
    String LOCAL_SERVICE_BUILD_YAML_KEY = "localService.build.yaml";
    String APPLICATION_PROPERTIES_FILE_NAME = "application.properties";
}
