package com.homo.core.maven;

import com.ctrip.framework.apollo.Config;
import com.ctrip.framework.apollo.ConfigService;
import com.ctrip.framework.apollo.core.enums.Env;
import com.ctrip.framework.apollo.core.spi.MetaServerProvider;
import com.homo.core.exend.utils.FileExtendUtils;
import com.homo.core.maven.apollo.CustomServerProvider;
import com.homo.core.maven.mojo.AbsHomoMojo;
import io.kubernetes.client.custom.IntOrString;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.openapi.models.*;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.model.Resource;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Data
@ToString(exclude = {"k8s_file"})
public class BuildConfiguration {
    public static AbsHomoMojo homoMojo;
    /////////////////////////////////////////////////////////////
    public String deploy_scan_class_path;
    public String deploy_service_scan_scope;
    public boolean deploy_filesystem_pvc_enable;
    public boolean deploy_apollo_update_enable;
    public boolean deploy_dns_update_enable;
    public String deploy_apollo_update_strategy;
    public Boolean local_debug;
    public String local_ip;


    /////////////////////////////////////////////////////////////
    public String apollo_meta = ConfigKey.EMPTY_CONFIG_VALUE;
    public String apollo_env;
    public String apollo_idc;
    public String apollo_addr;
    public String apollo_token;
    public String apollo_format;
    public String apollo_editor;
    public String apollo_auto_update_path;

    /////////////////////////////////////////////////////////////
    public String docker_repo_pull_addr;
    public String docker_repo_pull_dir;
    public String docker_repo_push_addr;
    public String docker_repo_push_dir;
    public String docker_repo_username;
    public String docker_repo_password;
    public String docker_repo_imageSuffix;
    public boolean docker_force_push;
    /////////////////////////////////////////////////////////////
    public String k8s_namespace;
    public String k8s_cert_config;
    public String k8s_file_name;
    public String deployment_temp_yaml = ConfigKey.DEPLOYMENT_TEMP_YAML;
    public String deployment_build_temp_yaml = ConfigKey.DEPLOYMENT_BUILD_YAML;
    public String statefulSet_temp_yaml = ConfigKey.STATEFUL_SET_TEMP_YAML;
    public String statefulSet_build_temp_yaml = ConfigKey.STATEFUL_SET_BUILD_YAML;
    public String headless_service_temp_yaml = ConfigKey.HEADLESS_SERVICE_TEMP_YAML;
    public String cluster_service_temp_yaml = ConfigKey.CLUSTER_SERVICE_TEMP_YAML;
    public String endpoint_temp_yaml = ConfigKey.ENDPOINTS_TEMP_YAML;
    public String cloud_service_build_temp_yaml = ConfigKey.CLOUD_SERVICE_BUILD_YAML;
    public String local_service_build_temp_yaml = ConfigKey.LOCAL_SERVICE_BUILD_YAML;
    public String cloud_service_file = ConfigKey.CLOUD_SERVICE_YAML;
    public String local_service_file = ConfigKey.LOCAL_SERVICE_YAML;
    public String pvc_temp_yaml = ConfigKey.PVC_TEMP_YAML;
    public String docker_file_temp = ConfigKey.DEPLOY_DOCKER_FILE_TEMP;
    public String docker_file_path = ConfigKey.DOCKER_TARGET_FILE_PATH;
    public File k8s_file;
    /////////////////////////////////////////////////////////////
    public String container_java_base_options = ConfigKey.CONTAINER_PARAM_JAVA_OPTIONS_DEFAULT_VALUE;
    public String container_request_cpu = ConfigKey.CONTAINER_PARAM_REQUEST_CPU_DEFAULT_VALUE;
    public String container_request_memory = ConfigKey.CONTAINER_PARAM_REQUEST_MEMORY_DEFAULT_VALUE;
    public String container_limit_cpu = ConfigKey.CONTAINER_PARAM_LIMIT_CPU_DEFAULT_VALUE;
    public String container_limit_memory = ConfigKey.CONTAINER_PARAM_LIMIT_MEMORY_DEFAULT_VALUE;
    /////////////////////////////////////////////////////////////
    public boolean sw_agent_enable;
    public String sw_backend_service = ConfigKey.SW_ENV_BACKEND_NAME_VALUE;
    /////////////////////////////////////////////////////////////
    public boolean volume_filesystem_enable;
    public String volume_filesystem_name;
    /////////////////////////////////////////////////////////////
    public boolean readiness_probe_enable;
    public String readiness_probe_type;
    public String readiness_probe_cmd;
    public String readiness_probe_tcp_port;
    public String readiness_probe_http_path;
    public int readiness_probe_http_port;
    /////////////////////////////////////////////////////////////
    public boolean prometheus_enable;
    public String prometheus_jmx_agent_exporter_ports;
    public String prometheus_labels;
    /////////////////////////////////////////////////////////////
    public int spec_pod_num;

    //名字缓存
    public static Map<String, Map<String, String>> nameMap = new HashMap<>();
    private static BuildConfiguration instant;
    private Properties systemProperties;
    private Properties applicationProperties;
    private Config apolloCustomerConfig;

    private BuildConfiguration() {
    }

    public static BuildConfiguration getInstance() {
        if (instant == null) {
            instant = new BuildConfiguration();
        }
        return instant;
    }
    public void init(AbsHomoMojo homoMojo) {
        BuildConfiguration.homoMojo = homoMojo;
        systemProperties = System.getProperties();
        try {
            Map<String, Properties> resourceProperties = getResourceProperties();
            applicationProperties = resourceProperties.get(ConfigKey.APPLICATION_PROPERTIES_FILE_NAME);
            apolloCustomerConfig = ConfigService.getConfig(ConfigKey.CUSTOM_BUILD_NS_VALUE);
        } catch (Exception e) {
            log.warn("init apolloCustomerConfig  {} fail", ConfigKey.CUSTOM_BUILD_NS_VALUE);
        }
        initApolloMeta();
        loadProperties();
    }
    public void initApolloMeta() {
        MetaServerProvider metaServerProvider = ServiceLoader.load(MetaServerProvider.class).iterator().next();
        CustomServerProvider serverProvider = CustomServerProvider.getInstance();
        String envType = serverProvider.getEnvType();
        if (StringUtils.isEmpty(envType)) {
            throw new RuntimeException("loadPropertiesFormApollo fail envType is empty");
        }
        //加载apollo配置
        apollo_meta = metaServerProvider.getMetaServerAddress(Env.fromString(envType));
        apollo_env = envType;
        apollo_idc = serverProvider.getDataCenter();
        log.info("apollo_meta {}", apollo_meta);
        log.info("apollo_env {}", apollo_env);
        log.info("apollo_idc {}", apollo_idc);
    }

    private void loadProperties() { 
        //调试配置
        local_debug = Boolean.parseBoolean(getProperty0(ConfigKey.LOCAL_DEBUG_KEY, ConfigKey.BOOLEAN_FALSE));
        local_ip = getProperty0(ConfigKey.LOCAL_IP_KEY, ConfigKey.EMPTY_CONFIG_VALUE);
        k8s_namespace = getProperty0(ConfigKey.K8S_NAMESPACE_KEY, apollo_idc);
        //类扫描配置
        deploy_scan_class_path = getProperty0(ConfigKey.SCAN_CLASS_PATH_KEY, ConfigKey.SCAN_CLASS_PATH_DEFAULT_VALUE);
        deploy_service_scan_scope = getProperty0(ConfigKey.SCAN_SCOPE_KEY, ConfigKey.DEFAULT_SCAN_SCOPE_DEFAULT_VALUE);
        //apollo配置
        apollo_addr = getProperty0(ConfigKey.APOLLO_ADDR_KEY, apollo_meta);
        apollo_token = getProperty0(ConfigKey.APOLLO_TOKEN_KEY, ConfigKey.EMPTY_CONFIG_VALUE);
        apollo_editor = getProperty0(ConfigKey.APOLLO_EDITOR_KEY, ConfigKey.APOLLO_EDITOR_DEFAULT_VALUE);
        apollo_format = getProperty0(ConfigKey.APOLLO_PROPERTY_FORMAT_VALUE, ConfigKey.APOLLO_PROPERTY_FORMAT_VALUE);
        deploy_apollo_update_enable = Boolean.parseBoolean(getProperty0(ConfigKey.DEPLOY_APOLLO_UPDATE_ENABLE_KEY, ConfigKey.BOOLEAN_FALSE));
        deploy_apollo_update_strategy = getProperty0(ConfigKey.APOLLO_UPDATE_STRATEGY_KEY, ConfigKey.APOLLO_UPDATE_STRATEGY_VALUE_SET_ABSENT);
        apollo_auto_update_path = getProperty0(ConfigKey.APOLLO_UPDATE_PATH, ConfigKey.APOLLO_UPDATE_PATH_DEFAULT_VALUE);
        //k8s模板文件配置
        deployment_temp_yaml = getProperty0(ConfigKey.DEPLOYMENT_TEMP_YAML_KEY, ConfigKey.DEPLOYMENT_TEMP_YAML);
        deployment_build_temp_yaml = getProperty0(ConfigKey.DEPLOYMENT_BUILD_YAML_KEY, ConfigKey.DEPLOYMENT_BUILD_YAML);
        statefulSet_temp_yaml = getProperty0(ConfigKey.STATEFUL_SET_TEMP_YAML_KEY, ConfigKey.STATEFUL_SET_TEMP_YAML);
        statefulSet_build_temp_yaml = getProperty0(ConfigKey.STATEFUL_SET_BUILD_YAML_KEY, ConfigKey.STATEFUL_SET_BUILD_YAML);
        headless_service_temp_yaml = getProperty0(ConfigKey.HEADLESS_SERVICE_TEMP_YAML_KEY, ConfigKey.HEADLESS_SERVICE_TEMP_YAML);
        cluster_service_temp_yaml = getProperty0(ConfigKey.CLUSTER_SERVICE_TEMP_YAML_KEY, ConfigKey.CLUSTER_SERVICE_TEMP_YAML);
        endpoint_temp_yaml = getProperty0(ConfigKey.ENDPOINTS_TEMP_YAML_KEY, ConfigKey.ENDPOINTS_TEMP_YAML);
        cloud_service_build_temp_yaml = getProperty0(ConfigKey.CLOUD_SERVICE_BUILD_YAML_KEY, ConfigKey.CLOUD_SERVICE_BUILD_YAML);
        local_service_build_temp_yaml = getProperty0(ConfigKey.LOCAL_SERVICE_BUILD_YAML_KEY, ConfigKey.LOCAL_SERVICE_BUILD_YAML);
        cloud_service_file = getProperty0(ConfigKey.CLOUD_SERVICE_BUILD_YAML_KEY, ConfigKey.CLOUD_SERVICE_YAML);
        local_service_file = getProperty0(ConfigKey.LOCAL_SERVICE_BUILD_YAML_KEY, ConfigKey.LOCAL_SERVICE_YAML);
        //加载k8s相关配置
        String k8s_cert_config_file_name = getProperty0(ConfigKey.K8S_CERT_CONFIG_FILE_KEY, ConfigKey.EMPTY_CONFIG_VALUE);
        if (ConfigKey.EMPTY_CONFIG_VALUE.equals(k8s_cert_config_file_name)) {
            k8s_cert_config = getProperty0(ConfigKey.K8S_CERT_CONFIG_KEY, ConfigKey.EMPTY_CONFIG_VALUE);
        } else {
            try {
                k8s_cert_config = FileExtendUtils.readCharacterFileToUtf8Str(k8s_cert_config_file_name, false);
            } catch (Exception e) {
                log.warn("k8s_cert_config load error {}", k8s_cert_config_file_name, e);
                k8s_cert_config = ConfigKey.EMPTY_CONFIG_VALUE;
            }
        }
        String k8s_file_name = getProperty0(ConfigKey.K8S_CONFIG_FILE_KEY, ConfigKey.K8S_CONFIG_FILE_DEFAULT_VALUE);
        if (ConfigKey.EMPTY_CONFIG_VALUE.equals(k8s_file_name)) {
            log.warn("k8s_file_name is null");
        } else {
            String pathName = homoMojo.getProject().getBasedir().toString() + File.separator + k8s_file_name;
            k8s_file = new File(pathName);
        }

        //加载镜像仓库配置
        docker_repo_pull_addr = getProperty0(ConfigKey.DOCKER_REPO_ADDR_KEY, ConfigKey.EMPTY_CONFIG_VALUE);
        docker_repo_pull_dir = getProperty0(ConfigKey.DOCKER_REPO_DIR_KEY, ConfigKey.EMPTY_CONFIG_VALUE);
        docker_repo_push_addr = getProperty0(ConfigKey.DOCKER_DEPLOY_ADDR_KEY, docker_repo_pull_addr);
        docker_repo_push_dir = getProperty0(ConfigKey.DOCKER_DEPLOY_DIR_KEY, docker_repo_pull_dir);
        docker_repo_username = getProperty0(ConfigKey.DOCKER_REPO_USERNAME_KEY, ConfigKey.EMPTY_CONFIG_VALUE);
        docker_repo_password = getProperty0(ConfigKey.DOCKER_REPO_PASSWORD_KEY, ConfigKey.EMPTY_CONFIG_VALUE);
        docker_repo_imageSuffix = getProperty0(ConfigKey.DOCKER_BUILD_SUFFIX_KEY, "");
        docker_force_push = Boolean.parseBoolean(getProperty0(ConfigKey.DOCKER_PUSH_KEY, ConfigKey.BOOLEAN_FALSE));
        docker_file_path = getProperty0(ConfigKey.DEPLOY_DOCKER_FILE_KEY, ConfigKey.DOCKER_TARGET_FILE_PATH);
        //容器启动参数  TODO 待优化，需要模块间各自独立
        container_java_base_options = getProperty0(ConfigKey.CONTAINER_PARAM_JAVA_OPTIONS_KEY, ConfigKey.CONTAINER_PARAM_JAVA_OPTIONS_DEFAULT_VALUE);
        container_request_cpu = getProperty0(ConfigKey.CONTAINER_PARAM_REQUEST_CPU_KEY, ConfigKey.CONTAINER_PARAM_REQUEST_CPU_DEFAULT_VALUE);
        container_request_memory = getProperty0(ConfigKey.CONTAINER_PARAM_REQUEST_MEMORY_KEY, ConfigKey.CONTAINER_PARAM_REQUEST_MEMORY_DEFAULT_VALUE);
        container_limit_cpu = getProperty0(ConfigKey.CONTAINER_PARAM_LIMIT_CPU_KEY, ConfigKey.CONTAINER_PARAM_LIMIT_CPU_DEFAULT_VALUE);
        container_limit_memory = getProperty0(ConfigKey.CONTAINER_PARAM_LIMIT_MEMORY_KEY, ConfigKey.CONTAINER_PARAM_LIMIT_MEMORY_DEFAULT_VALUE);
        //skywalking
        sw_agent_enable = Boolean.parseBoolean(getProperty0(ConfigKey.SW_TRACING_ENABLE_KEY, ConfigKey.BOOLEAN_FALSE));
        sw_backend_service = getProperty0(ConfigKey.SW_ENV_BACKEND_SERVICE_KEY, ConfigKey.SW_ENV_BACKEND_NAME_VALUE);
        //prometheus
        prometheus_enable = Boolean.parseBoolean(getProperty0(ConfigKey.PROMETHEUS_ENABLE, ConfigKey.BOOLEAN_FALSE));
        prometheus_jmx_agent_exporter_ports = getProperty0(ConfigKey.PROMETHEUS_EXPORT_PORTS, ConfigKey.PROMETHEUS_EXPORT_PORT_VALUE);
        prometheus_labels = getProperty0(ConfigKey.PROMETHEUS_LABELS, ConfigKey.PROMETHEUS_LABEL_VALUE);
        //文件系统
        deploy_dns_update_enable = Boolean.parseBoolean(getProperty0(ConfigKey.DEPLOY_DNS_UPDATE_ENABLE_KEY, ConfigKey.BOOLEAN_FALSE));
        volume_filesystem_enable = Boolean.parseBoolean(getProperty0(ConfigKey.K8S_FILESYSTEM_VOLUME_ENABLE_KEY, ConfigKey.BOOLEAN_FALSE));
        volume_filesystem_name = getProperty0(ConfigKey.K8S_FILESYSTEM_VOLUME_NAME_KEY, ConfigKey.K8S_FILESYSTEM_VOLUME_NAME_DEFAULT_VALUE);
        //健康检查
        readiness_probe_enable = Boolean.parseBoolean(getProperty0(ConfigKey.READINESS_PROBE_CHECK_ENABLE_KEY, ConfigKey.BOOLEAN_FALSE));
        readiness_probe_type = getProperty0(ConfigKey.READINESS_PROBE_TYPE_KEY, ConfigKey.READINESS_PROBE_TYPE_CMD);
        readiness_probe_cmd = getProperty0(ConfigKey.READINESS_PROBE_CMD_KEY, ConfigKey.READINESS_PROBE_CMD_DEFAULT_VALUE);
        readiness_probe_tcp_port = getProperty0(ConfigKey.READINESS_PROBE_TYPE_TCP_PORT_KEY, ConfigKey.READINESS_PROBE_TYPE_TCP_PORT_DEFAULT_VALUE);
        readiness_probe_http_path = getProperty0(ConfigKey.READINESS_PROBE_TYPE_HTTP_PATH_KEY, ConfigKey.READINESS_PROBE_TYPE_HTTP_PATH_DEFAULT_VALUE);
        readiness_probe_http_port = Integer.parseInt(getProperty0(ConfigKey.READINESS_PROBE_TYPE_HTTP_PORT_KEY, ConfigKey.READINESS_PROBE_TYPE_HTTP_PORT_DEFAULT_VALUE));
        //pod数量
        spec_pod_num = Integer.parseInt(getProperty0(ConfigKey.SPEC_POD_NUM, ConfigKey.DEFAULT_POD_NUM));
    }

    public static V1Service createHeadlessServiceTemp(HomoServiceSetter serviceSetter) throws IOException {
        V1Service service = FileExtendUtils.readYamlToObj(instant.headless_service_temp_yaml, V1Service.class, true);
        Map<String, String> selector = service.getSpec().getSelector();
        if (selector != null) {
            selector.put(ConfigKey.APP, getAppName());
        }
        V1ObjectMeta metadata = service.getMetadata();
        if (serviceSetter.isStateful()) {
            if (metadata.getLabels() == null) {
                metadata.setLabels(new HashMap<>());
            }
            metadata.getLabels().put(ConfigKey.STATEFUL_LABELS, serviceSetter.getServiceName());
        }
        metadata.setName(serviceSetter.getServiceName());
        V1ServicePort servicePort = service.getSpec().getPorts().get(0);
        Integer port = serviceSetter.getServicePort();
        servicePort.setPort(port);
        servicePort.setTargetPort(new IntOrString(port));
        return service;
    }



    public V1PersistentVolumeClaim createPvcTemp() throws IOException {
        String pcvName = instant.getK8s_namespace() + ConfigKey.FILE_SYSTEM_PVC_SUFFIX;
        V1PersistentVolumeClaim pvc = FileExtendUtils.readYamlToObj(pvc_temp_yaml, V1PersistentVolumeClaim.class, true);
        V1ObjectMeta metadata = pvc.getMetadata();
        metadata.setName(pcvName);
        String k8sNamespace = instant.getK8s_namespace();
        metadata.getLabels().put(ConfigKey.PVC_SUB_DIR_LABEL, k8sNamespace);
        metadata.setNamespace(k8sNamespace);
        return pvc;
    }

    public V1Service createClusterServiceTemp(HomoServiceSetter serviceSetter) throws IOException {
        V1Service service = FileExtendUtils.readYamlToObj(instant.cluster_service_temp_yaml, V1Service.class, true);
        Map<String, String> selector = service.getSpec().getSelector();
        if (selector != null) {
            selector.put(ConfigKey.APP, getAppName());
        }
        service.getMetadata().setName(serviceSetter.getServiceName());
        V1ServicePort servicePort = service.getSpec().getPorts().get(0);
        servicePort.setPort(serviceSetter.getServicePort());
        servicePort.setTargetPort(new IntOrString(serviceSetter.getServicePort()));
        return service;
    }

    public V1Endpoints createEndPointsTemp(HomoServiceSetter serviceSetter) throws IOException {
        V1Endpoints endpoint = FileExtendUtils.readYamlToObj(instant.endpoint_temp_yaml, V1Endpoints.class, true);
        endpoint.getMetadata().setName(serviceSetter.getServiceName());
        V1EndpointPort subPort = endpoint.getSubsets().get(0).getPorts().get(0);
        subPort.setPort(serviceSetter.getServicePort());
        return endpoint;
    }
    public Map<String, Properties> getResourceProperties(){
        List<Resource> resources = homoMojo.getProject().getResources();
        Map<String, Properties> propertiesMap = new HashMap<>();
        for (Resource resource : resources) {
            File file = new File(resource.getDirectory());
            if (file.exists()) {
                File[] files = file.listFiles();
                if (files != null) {
                    for (File file1 : files) {
                        if (file1.getName().endsWith(".properties")) {
                            try {
                                Properties properties = new Properties();
                                properties.load(file1.toURI().toURL().openStream());
                                propertiesMap.put(file1.getName(), properties);
                            } catch (IOException e) {
                                log.error("getApplicationPropertiesFile load {} fail", file1.getName());
                            }
                        }
                    }
                }
            }
        }
        return propertiesMap;
    }


    public String getProperty0(String key, String defaultValue) {
        String value;
        if (systemProperties.contains(key)) {
            value = systemProperties.getProperty(key, defaultValue);
        } else if (applicationProperties.getProperty(key) != null) {
            value = applicationProperties.getProperty(key, defaultValue);
        } else if (apolloCustomerConfig != null && !Objects.equals(apolloCustomerConfig.getProperty(key, defaultValue), defaultValue)) {
            value = apolloCustomerConfig.getProperty(key, defaultValue);
        } else {
            value = defaultValue;
        }
        log.info("properties {} value {} defaultValue {}", key, value, defaultValue);
        return value;
    } 

    public void appendDeployTemplateInfo(V1PodTemplateSpec template) {
        V1Container container = template.getSpec().getContainers().get(0);
        //设置镜像
        container.setImage(getPushImageName());
        //机器配置
        setCpuAndMem(container);
        //prometheus
        setPrometheus(template);
        //java启动参数
        setJavaOptions(container);
        //skyWalking
        setSkyWalking(template);
        setFileSystemVolume(template);
        setReadinessProbe(container);
    }

    public String getK8sConfig() throws IOException {
        String kubeConfigStr = null;
        if (!ConfigKey.EMPTY_CONFIG_VALUE.equals(k8s_cert_config)) {
            kubeConfigStr = k8s_cert_config;
        } else if (k8s_file != null && k8s_file.exists()) {
            kubeConfigStr = FileExtendUtils.readCharacterFileToUtf8Str(k8s_file);
        }
        return kubeConfigStr;
    }

    private void setReadinessProbe(V1Container container) {
        if (readiness_probe_enable) {
            V1Probe probe = container.getReadinessProbe();
            if (probe == null) {
                probe = new V1Probe();
                container.setReadinessProbe(probe);
            }
            String checkType = readiness_probe_type;
            probe.setFailureThreshold(3);
            probe.setInitialDelaySeconds(10);
            probe.setPeriodSeconds(10);
            probe.setSuccessThreshold(1);
            probe.setTimeoutSeconds(10);
            if (ConfigKey.READINESS_PROBE_TYPE_HTTP.equals(checkType)) {
                V1HTTPGetAction httpGet = new V1HTTPGetAction();
                httpGet.setPath(readiness_probe_http_path);
                httpGet.setPort(new IntOrString(readiness_probe_http_port));
                probe.setHttpGet(httpGet);
            } else if (ConfigKey.READINESS_PROBE_TYPE_CMD.equals(checkType)) {
                V1ExecAction exec = new V1ExecAction();
                String[] commands = readiness_probe_cmd.split("\\s+");
                exec.setCommand(Arrays.asList(commands));
                probe.setExec(exec);
            } else if (ConfigKey.READINESS_PROBE_TYPE_TCP.equals(checkType)) {
                V1TCPSocketAction tcp = new V1TCPSocketAction();
                tcp.setPort(new IntOrString(readiness_probe_http_port));
                probe.setTcpSocket(tcp);
            } else {
                log.error("deploy_readiness_probe_type {} not support", checkType);
            }
        }
    }

    public String getFileSystemPvcName() {
        return k8s_namespace + ConfigKey.K8S_FILESYSTEM_PVC_SUFFIX;
    }



    private void setFileSystemVolume(V1PodTemplateSpec template) {
        if (volume_filesystem_enable) {
            //先去除,再加上
            removeVolume(template, volume_filesystem_name);
            log.info("volume_filesystem_enable is true");
            V1PersistentVolumeClaimVolumeSource pvcSource = new V1PersistentVolumeClaimVolumeSource();
            pvcSource.setClaimName(getFileSystemPvcName());
            V1Volume volume = new V1Volume();
            volume.setName(volume_filesystem_name);
            volume.setPersistentVolumeClaim(pvcSource);
            V1VolumeMount volumeMount = new V1VolumeMount();
            volumeMount.setName(volume_filesystem_name);
            volumeMount.setMountPath(ConfigKey.K8S_FILESYSTEM_VOLUME_MOUNT);
            volumeMount.setSubPath(null);
            template.getSpec().getVolumes().add(volume);
            template.getSpec().getContainers().get(0).getVolumeMounts().add(volumeMount);
        } else {
            log.info("volume_filesystem_enable is false");
        }
    }


    public void removeVolume(V1PodTemplateSpec template, String volume) {
        V1Container container = template.getSpec().getContainers().get(0);
        List<V1VolumeMount> volumeMounts = container.getVolumeMounts();
        List<V1Volume> volumes = template.getSpec().getVolumes();
        if (volumeMounts == null) {
            volumeMounts = new ArrayList<>();
            container.setVolumeMounts(volumeMounts);
        }
        if (volumes == null) {
            volumes = new ArrayList<>();
            template.getSpec().setVolumes(volumes);
        }
        volumeMounts.removeIf(v1VolumeMount -> v1VolumeMount.getName().equals(volume));
        volumes.removeIf(v1Volume -> v1Volume.getName().equals(volume));
    }

    private void setSkyWalking(V1PodTemplateSpec template) {
        V1EnvVarBuilder envVarBuilder = new V1EnvVarBuilder();
        V1EnvVar[] envArray = new V1EnvVar[3];
        envArray[0] = envVarBuilder.withName(ConfigKey.SW_ENV_BACKEND_SERVICE_KEY).withValue(sw_backend_service).build();
        envArray[1] = envVarBuilder.withName(ConfigKey.SW_ENV_LOG_DIR_KEY).withValue(ConfigKey.SW_ENV_LOG_DIR_VALUE).build();
        envArray[2] = envVarBuilder.withName(ConfigKey.SW_ENV_AGENT_NAME_KEY).withValue(getSwAgentName()).build();
        V1Container container = template.getSpec().getContainers().get(0);
        if (sw_agent_enable) {
            log.info("skyWalking enable");
            addEnv(container, Arrays.asList(envArray));
            appendCommand(container, ConfigKey.SW_PARAM_JAVA_OPTIONS);
        } else {
            log.info("skyWalking disable");
            removeEnv(container, Arrays.asList(envArray));
            removeCommand(container, ConfigKey.SW_PARAM_JAVA_OPTIONS);
            removeVolume(template, container, ConfigKey.SW_PVC_VOLUME_NAME);
            removeVolume(template, container, ConfigKey.SW_CONFIG_MAP_VOLUME_NAME);
        }
    }

    public static void appendContainerLabelInfo(V1PodTemplateSpec template, String key, String value) {
        V1ObjectMeta metadata = template.getMetadata();
        if (metadata.getLabels() == null) {
            metadata.setLabels(new HashMap<>());
        }
        metadata.getLabels().put(key, value);
    }
    public void removeVolume(V1PodTemplateSpec templateSpec, V1Container container, String volumeName) {
        List<V1Volume> volumes = templateSpec.getSpec().getVolumes();
        List<V1VolumeMount> volumeMounts = container.getVolumeMounts();
        if (volumes == null || volumes.size() == 0) {
            log.info("volumes empty, no need to delete");
        } else {
            volumes.removeIf(v1Volume -> v1Volume.getName().equals(volumeName));
        }
        if (volumeMounts == null || volumeMounts.size() == 0) {
            log.info("volumeMounts empty, no need to delete");
        } else {
            volumeMounts.removeIf(v1VolumeMount -> v1VolumeMount.getName().equals(volumeName));
        }
    }

    /**
     * 去除env
     */
    public static void removeEnv(V1Container container, List<V1EnvVar> envs) {
        List<V1EnvVar> toRemoveEnv = container.getEnv();
        if (toRemoveEnv == null || toRemoveEnv.size() == 0) {
            log.info("env empty, no need to delete");
        } else {
            for (V1EnvVar envVar : envs) {
                toRemoveEnv.removeIf(v1EnvVar -> v1EnvVar.getName().equals(envVar.getName()));
            }
        }
    }

    /**
     * 更新env
     */
    public static void addEnv(V1Container container, List<V1EnvVar> envs) {
        removeEnv(container, envs);
        List<V1EnvVar> toAddEnv = container.getEnv();
        if (toAddEnv == null) {
            toAddEnv = new ArrayList<>();
        }
        toAddEnv.addAll(envs);
        container.setEnv(toAddEnv);
    }

    private String getSwAgentName() {
        return getK8s_namespace() + "." + homoMojo.getProjectName();
    }

    private void setJavaOptions(V1Container container) {
        String[] options = container_java_base_options.trim().split("\\s+");
        List<String> containerCommand = container.getCommand();
        if (containerCommand == null) {
            containerCommand = new ArrayList<>();
            container.setCommand(containerCommand);
        }
        for (String option : options) {
            appendCommand(container, option);
        }
        appendCommand(container,"app.jar");
    }

    private void setPrometheus(V1PodTemplateSpec template){
        V1Container container = template.getSpec().getContainers().get(0);
        if (prometheus_enable){
            List<String> containerCommand = container.getCommand();
            if (containerCommand == null) {
                containerCommand = new ArrayList<>();
                container.setCommand(containerCommand);
            }
            appendCommand(container, ConfigKey.PROMETHEUS_EXPORT_JAVA_AGENT_VALUE);
        }
        String[] ports = prometheus_jmx_agent_exporter_ports.split(",");
        for (String port : ports) {
            String[] split  = port.split(":");
            V1ContainerPort containerPort = new V1ContainerPort();
            containerPort.setContainerPort(Integer.parseInt(split[0]));
            if (split.length > 1) {
                containerPort.setName(split[1].trim());
            }
            appendContainerExportPortInfo(container, containerPort);
        }
        String[] labels= prometheus_labels.split(",");
        for (String label : labels) {
            String[] split = label.split(":");
            appendContainerLabelInfo(template, split[0], split[1].trim());
        }
    }

    public void appendCommand(V1Container container, String addCommand) {
        List<String> commands = container.getCommand();
        if (commands == null) {
            commands = new ArrayList<>();
        }
        for (String command : commands) {
            if (command.contains(addCommand)) {
                return;
            }
        }
        commands.add(addCommand);
    }

    public void removeCommand(V1Container container, String addCommand) {
        List<String> commands = container.getCommand();
        if (commands == null) {
            return;
        }
        commands.removeIf(command -> command.contains(addCommand));
    }

    private void setCpuAndMem(V1Container container) {
        Map<String, Quantity> requests = container.getResources().getRequests();
        Map<String, Quantity> limits = container.getResources().getLimits();
        requests.put("cpu", new Quantity(container_request_cpu));
        requests.put("memory", new Quantity(container_request_memory));
        limits.put("cpu", new Quantity(container_limit_cpu));
        limits.put("memory", new Quantity(container_limit_memory));
    }
    public String getApolloUpdatePath() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), apollo_auto_update_path);
    }
    public String getPvcFileName() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), ConfigKey.PVC_YAML);
    }

    public String getStatefulSetFileName() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), ConfigKey.DEPLOY_STATEFUL_FILE);
    }

    public String getDeploymentFileName() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), ConfigKey.DEPLOY_DEPLOYMENT_FILE);
    }

    public String getLocalServiceBuildFile() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), local_service_build_temp_yaml);
    }

    public String getCloudServiceBuildFile() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), cloud_service_build_temp_yaml);
    }

    public String getLocalServiceFile() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), local_service_file);
    }

    public String getCloudServiceFile() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), cloud_service_file);
    }

    public enum PropertySource {
        DEFAULT,
        PROPERTY,
        APOLLO
    }

    public V1Deployment createDeploymentTemp() throws IOException {
        String deploymentTempYaml = deployment_temp_yaml;
        boolean absolutePath = FileExtendUtils.isAbsolutePath(deploymentTempYaml);
        V1Deployment deployment = FileExtendUtils.readYamlToObj(deploymentTempYaml, V1Deployment.class, !absolutePath);
        return deployment;
    }

    public V1StatefulSet createStatefulSetTemp() throws IOException {
        String statefulSetTempYaml = statefulSet_temp_yaml;
        boolean absolutePath = FileExtendUtils.isAbsolutePath(statefulSetTempYaml);
        V1StatefulSet statefulSet = FileExtendUtils.readYamlToObj(statefulSetTempYaml, V1StatefulSet.class, !absolutePath);
        return statefulSet;
    }

    public static String containerName() {
        return nameMap.computeIfAbsent(homoMojo.getProjectName(), k -> new HashMap<>()).
                computeIfAbsent(ConfigKey.CONTAINER, k -> homoMojo.getProjectName() + "-container");
    }

    public static String getAppName() {
        return nameMap.computeIfAbsent(homoMojo.getProjectName(), k -> new HashMap<>()).
                computeIfAbsent(ConfigKey.APP, k -> homoMojo.getProjectName() + "-app");
    }

    public static String getDeploymentName() {
        return nameMap.computeIfAbsent(homoMojo.getProjectName(), k -> new HashMap<>()).
                computeIfAbsent(ConfigKey.DEPLOYMENT, k -> homoMojo.getProjectName() + "-deployment");
    }

    public String getDeploymentBuildYaml() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), deployment_build_temp_yaml);
    }

    public String getDevopsRootDir() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), "devops");
    }


    public String getStatefulSetBuildYaml() {
        return FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), statefulSet_build_temp_yaml);
    }

    private static void generateStatefulSetFile(V1StatefulSet statefulSetTemp) {
        MavenProject project = homoMojo.getProject();
        //match label
    }

    /**
     * 镜像名由两部分组成：仓库名和标签（tag）。例如：repository:tag
     * 仓库名可以包含多个路径段，类似于文件系统路径。通常使用命名空间的方式来组织镜像，例如：namespace/repository:tag
     * 仓库名中的字母字符必须全部为小写，并且只能包含数字、小写字母、连字符（-）和下划线（_）。
     * 标签是可选的，如果没有提供标签，默认使用 latest 标签。
     * 标签通常用于指示版本号或者特定的构建。例如：1.0, v2, stable等。
     *
     * @return
     */
    public String getPushImageName() {
        String imageName = getImageNameWithRepo() + ":" + getImageTag();
        return imageName;
    }

    public String getImageNameWithRepo() {
        String getImageName = getDocker_repo_push_addr() + "/" + getDocker_repo_push_dir() + "/" + homoMojo.getProjectName();
        return getImageName.toLowerCase();
    }

    public String getImageTag() {
        String tag = homoMojo.getProject().getVersion() + getDocker_repo_imageSuffix();
        return tag;
    }

    public String getTargetDockerFilePath() {
        String dockerFilePath = FileExtendUtils.mergePath(homoMojo.getProject().getBasedir().toString(), ConfigKey.DOCKER_TARGET_FILE_PATH);
        return dockerFilePath;
    }

    public String getStatefulService0Domain(String serviceName) {
        return serviceName + "-0." + serviceName + "." + getK8s_namespace() + ConfigKey.DOMAIN_SUFFIX;
    }

    public static void appendDeploymentLabelsInfo(V1Deployment deploymentTemp) {
        //match label
        Map<String, String> matchLabels = deploymentTemp.getSpec().getSelector().getMatchLabels();
        matchLabels.put(ConfigKey.APP, getAppName());
        //template
        Map<String, String> template_labels = deploymentTemp.getSpec().getTemplate().getMetadata().getLabels();
        template_labels.put(ConfigKey.APP, getAppName());
        //container
        V1Container container = deploymentTemp.getSpec().getTemplate().getSpec().getContainers().get(0);
        container.setName(containerName());
    }

    public void appendStatefulMetaInfo(V1ObjectMeta metadata) {
        /**
         * 无状态服务设置名字
         * 有状态只能和服务名同名,故无法设置
         */
        metadata.setNamespace(getK8s_namespace());
    }

    public void appendDeploymentMetaInfo(V1ObjectMeta metadata) {
        /**
         * 无状态服务设置名字
         * 有状态只能和服务名同名,故无法设置
         */
        metadata.setName(getDeploymentName());
        metadata.setNamespace(getK8s_namespace());
    }

    public void appendContainerInfo(V1Container container) {
        /**
         * 设置镜像信息
         */
        container.setImage(getPushImageName());
        /**
         * 配置资源
         */
    }

    public static void appendDeploymentLabelsInfo(V1StatefulSet statefulSet) {
        //match label
        Map<String, String> matchLabels = statefulSet.getSpec().getSelector().getMatchLabels();
        matchLabels.put(ConfigKey.APP, getAppName());
        //template
        Map<String, String> template_labels = statefulSet.getSpec().getTemplate().getMetadata().getLabels();
        template_labels.put(ConfigKey.APP, getAppName());
        //container
        V1Container container = statefulSet.getSpec().getTemplate().getSpec().getContainers().get(0);
        container.setName(containerName());
    }


    public static void appendContainerEnvInfo(V1Container container) {
        //设置groupId,artifactId,version环境变量
        List<V1EnvVar> envVarList = new ArrayList<>();
        envVarList.add(new V1EnvVar().name(ConfigKey.ENV_GROUP).value(homoMojo.getProject().getGroupId()));
        envVarList.add(new V1EnvVar().name(ConfigKey.ENV_ARTIFACT).value(homoMojo.getProject().getArtifactId()));
        envVarList.add(new V1EnvVar().name(ConfigKey.ENV_VERSION).value(homoMojo.getProject().getVersion()));
        replaceContainerEnvInfo(container, envVarList);
        addContainerEnvInfo(container, envVarList);
    }

    private static void addContainerEnvInfo(V1Container container, List<V1EnvVar> envs) {
        List<V1EnvVar> toAddEnv = container.getEnv();
        if (toAddEnv == null) {
            toAddEnv = new ArrayList<>();
        }
        toAddEnv.addAll(envs);
        container.setEnv(toAddEnv);
    }

    private static void replaceContainerEnvInfo(V1Container container, List<V1EnvVar> envs) {
        List<V1EnvVar> envVarList = container.getEnv();
        if (envVarList == null || envVarList.isEmpty()) {
            log.info("env empty, no need to delete");
        } else {
            for (V1EnvVar v1EnvVar : envs) {
                envVarList.removeIf(item -> item.getName().equals(v1EnvVar.getName()));
            }
        }
    }

    public static void appendContainerExportPortInfo(V1Container container,V1ContainerPort port) {
        List<V1ContainerPort> ports = container.getPorts();
        if (ports == null) {
            ports = new ArrayList<>();
        }
        ports.add(port);
        container.setPorts(ports);
    }


}
