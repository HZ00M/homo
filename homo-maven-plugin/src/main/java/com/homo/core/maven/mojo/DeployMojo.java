package com.homo.core.maven.mojo;

import com.github.dockerjava.api.command.BuildImageResultCallback;
import com.github.dockerjava.api.exception.DockerException;
import com.homo.core.exend.client.ApolloExtendClient;
import com.homo.core.exend.client.DockerExtendClient;
import com.homo.core.exend.client.K8sExtendClient;
import com.homo.core.exend.utils.CommandUtils;
import com.homo.core.exend.utils.FileExtendUtils;
import com.homo.core.maven.BuildConfiguration;
import com.homo.core.maven.ConfigKey;
import io.kubernetes.client.common.KubernetesObject;
import io.kubernetes.client.common.KubernetesType;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.models.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.*;

@Slf4j
@Mojo(name = "homoDeploy",
        requiresDependencyResolution = ResolutionScope.NONE)
public class DeployMojo extends AbsHomoMojo<DeployMojo> {
    private BuildConfiguration buildConfiguration;
    private K8sExtendClient k8sExtendClient;
    private ApolloExtendClient apolloExtendClient;
    private DockerExtendClient dockerExtendClient;
    private V1Deployment deployment;
    private V1StatefulSet statefulSet;
    private Boolean isStatefulService;

    @Override
    protected void doExecute() throws MojoFailureException {
        try {
            //初始化配置
            initConfig();
            //更新apollo配置 todo 待验证
            updateApollo();
            //检查k8s命名空间
            checkAndCreateK8sNamespace();
            //检查file system pvc todo 待验证
            checkAndCreatePvc();
            //更新apollo configMap配置文件
            updateApolloConfigMap();
            //更新dns服务配置文件
            updateDnsConfigMap();
            //更新镜像
            updateImage();
            //更新k8s配置文件
            updateK8sDeployFile();
            //更新服务发现配置文件
            updateDiscoveryFile();
            //部署应用
            deployApplication();
            //服务发现
            updateDiscover();

        }catch (DockerException dockerException) {
            log.error("homoDeploy error StackTrace {} ", dockerException.getStackTrace(),dockerException);
            throw new MojoFailureException("dockerException error", dockerException);
        }catch (ApiException apiException){
            log.error("homoDeploy error StackTrace {} ", apiException.getResponseBody(),apiException);
            throw new MojoFailureException("apiException error", apiException);
        }catch (Exception e) {
            log.error("homoDeploy error", e);
            throw new MojoFailureException("Exception error", e);
        }

    }

    private boolean isStatefulService() {
        if (isStatefulService == null) {
            String filePath = buildConfiguration.getStatefulSetBuildYaml();
            File statefulBuildFile = new File(filePath);
            isStatefulService = statefulBuildFile.exists() && statefulBuildFile.canRead();
        }
        return isStatefulService;
    }

    private String getStatefulName() throws IOException {
        if (!isStatefulService()) {
            return null;
        }
        return FileExtendUtils.readYamlToObj(buildConfiguration.getStatefulSetFileName(), V1StatefulSet.class, false).getMetadata().getName();
    }

    private void updateDiscover() throws ApiException, IOException {
        String serviceDeployFile;
        if (buildConfiguration.local_debug) {
            serviceDeployFile = buildConfiguration.getLocalServiceFile();
        } else {
            serviceDeployFile = buildConfiguration.getCloudServiceFile();
        }
        if (isStatefulService() && buildConfiguration.local_debug) {
            log.info("updateDiscover isStatefulService {} local_debug {} skip update serviceDeployFile", isStatefulService, buildConfiguration.local_debug);
            //有状态服务在本地调试下需要关闭远端服务才能生效...
            k8sExtendClient.findAndDeleteStatefulSet(buildConfiguration.getK8s_namespace(), getStatefulName());
        } else {
            List<KubernetesType> kubeObjs = new ArrayList<>();
            try {
                kubeObjs = FileExtendUtils.readFileToK8sObjs(serviceDeployFile, false);
            } catch (IOException e) {
                log.warn("discoveryService serviceDeployFile serviceDeployFile {} fail", serviceDeployFile, e);
            }
            for (KubernetesType kubeObj : kubeObjs) {
                if (kubeObj instanceof V1Service) {
                    V1Service service = (V1Service) kubeObj;
                    k8sExtendClient.updateService(buildConfiguration.getK8s_namespace(), service, false);
                }
                if (kubeObj instanceof V1Endpoints) {
                    V1Endpoints endpoints = (V1Endpoints) kubeObj;
                    k8sExtendClient.updateEndpoints(buildConfiguration.getK8s_namespace(), endpoints);
                }
            }
        }

    }

    private void updateDiscoveryFile() throws IOException {
        String serviceBuildFile;
        String serviceDeployFile;
        if (buildConfiguration.local_debug) {
            serviceBuildFile = buildConfiguration.getLocalServiceBuildFile();
            serviceDeployFile = buildConfiguration.getLocalServiceFile();
        } else {
            serviceBuildFile = buildConfiguration.getCloudServiceBuildFile();
            serviceDeployFile = buildConfiguration.getCloudServiceFile();
        }
        List<KubernetesType> kubeObjs = new ArrayList<>();
        try {
            kubeObjs = FileExtendUtils.readFileToK8sObjs(serviceBuildFile, false);
        } catch (IOException e) {
            log.error("discoveryService error", e);
        }
        if (kubeObjs.size() > 0) {
            for (KubernetesType kubeObj : kubeObjs) {
                if (kubeObj instanceof V1Endpoints) {
                    V1Endpoints endpoints = (V1Endpoints) kubeObj;
                    V1EndpointAddress endpointAddress = endpoints.getSubsets().get(0).getAddresses().get(0);
                    endpointAddress.setIp(buildConfiguration.getLocal_ip());
                }
                if (kubeObj instanceof KubernetesObject) {
                    KubernetesObject kubernetesObject = (KubernetesObject) kubeObj;
                    kubernetesObject.getMetadata().setNamespace(buildConfiguration.getK8s_namespace());
                }
            }
            FileExtendUtils.writeYamlObjToFile(serviceDeployFile, kubeObjs.iterator());
        }

    }

    private void deployApplication() throws ApiException, IOException {
        String namespace = buildConfiguration.getK8s_namespace();
        if (!buildConfiguration.local_debug) {
            if (isStatefulService()) {
                V1StatefulSet statefulSet = FileExtendUtils.readYamlToObj(buildConfiguration.getStatefulSetFileName(), V1StatefulSet.class, false);
                String statefulSetName = statefulSet.getMetadata().getName();
                String fileSelector = String.format("metadata.name=%s", statefulSetName);
                V1StatefulSetList statefulSetList = k8sExtendClient.appsV1Api.listNamespacedStatefulSet(namespace, null, false,
                        null, fileSelector, null, 1, null, null, false);
                if (statefulSetList == null || statefulSetList.getItems().size() == 0) {
                    log.info("deployApplication create statefulSet {}", statefulSetName);
                    k8sExtendClient.appsV1Api.createNamespacedStatefulSet(buildConfiguration.getK8s_namespace(), statefulSet, null, null, null);
                } else {
                    V1StatefulSet oldStatefulSet = statefulSetList.getItems().get(0);
                    if (compatiable(statefulSet, oldStatefulSet)) {
                        log.info("deployApplication update statefulSet {}", statefulSetName);
                        k8sExtendClient.appsV1Api.replaceNamespacedStatefulSet(statefulSetName, namespace, statefulSet, null, null, null);
                    } else {
                        log.info("deployApplication statefulSet {} is not compatiable,delete oldStatefulSet ", statefulSetName);
                        k8sExtendClient.appsV1Api.deleteNamespacedStatefulSet(statefulSetName, namespace, null, null, 1, null, null, null);
                        k8sExtendClient.appsV1Api.createNamespacedStatefulSet(namespace, statefulSet, null, null, null);
                    }
                }
            } else {
                V1Deployment v1Deployment = FileExtendUtils.readYamlToObj(buildConfiguration.getStatefulSetFileName(), V1Deployment.class, false);
                String deploymentName = v1Deployment.getMetadata().getName();
                String fileSelector = String.format("metadata.name=%s", deploymentName);
                V1DeploymentList deploymentList = k8sExtendClient.appsV1Api.listNamespacedDeployment(namespace, null, false,
                        null, fileSelector, null, 1, null, null, false);
                if (deploymentList == null || deploymentList.getItems().size() == 0) {
                    log.info("deployApplication create deployment {}", deploymentName);
                    k8sExtendClient.appsV1Api.createNamespacedDeployment(buildConfiguration.getK8s_namespace(), v1Deployment, null, null, null);
                } else {
                    log.info("deployApplication update deployment {}", deploymentName);
                    k8sExtendClient.appsV1Api.replaceNamespacedDeployment(deploymentName, namespace, v1Deployment, null, null, null);
                }
            }
        } else {
            log.info("deployApplication is debug model");
        }
    }

    //有状态更新check
    //updates to statefulset spec for fields other than 'replicas', 'template', and 'updateStrategy' are forbidden
    public static boolean compatiable(V1StatefulSet statefulSet1, V1StatefulSet statefulSet2) {
        String podManagementPolicy1 = statefulSet1.getSpec().getPodManagementPolicy();
        String podManagementPolicy2 = statefulSet2.getSpec().getPodManagementPolicy();
        String matchLabels1 = statefulSet1.getSpec().getSelector().getMatchLabels().get("app");
        String matchLabels2 = statefulSet2.getSpec().getSelector().getMatchLabels().get("app");
        String serviceName1 = statefulSet1.getSpec().getServiceName();
        String serviceName2 = statefulSet2.getSpec().getServiceName();
        return Objects.equals(podManagementPolicy1, podManagementPolicy2)
                && Objects.equals(matchLabels1, matchLabels2)
                && Objects.equals(serviceName1, serviceName2);
    }

    private void updateK8sDeployFile() throws IOException {
        if (isStatefulService) {
            statefulSet = FileExtendUtils.readYamlToObj(buildConfiguration.getStatefulSetBuildYaml(), V1StatefulSet.class, false);
            V1PodTemplateSpec template = statefulSet.getSpec().getTemplate();
            V1ObjectMeta metadata = statefulSet.getMetadata();
            buildConfiguration.appendStatefulMetaInfo(metadata);
            buildConfiguration.appendDeployTemplateInfo(template);
            statefulSet.getSpec().setReplicas(buildConfiguration.spec_pod_num);
            FileExtendUtils.writeK8sObjToFile(buildConfiguration.getStatefulSetFileName(), statefulSet);
        } else {
            deployment = FileExtendUtils.readYamlToObj(buildConfiguration.getDeploymentBuildYaml(), V1Deployment.class, false);
            V1PodTemplateSpec template = deployment.getSpec().getTemplate();
            V1ObjectMeta metadata = deployment.getMetadata();
            buildConfiguration.appendDeploymentMetaInfo(metadata);
            buildConfiguration.appendDeployTemplateInfo(template);
            deployment.getSpec().setReplicas(buildConfiguration.spec_pod_num);
            FileExtendUtils.writeK8sObjToFile(buildConfiguration.getDeploymentFileName(), deployment);
        }
    }

    private void updateImage() throws IOException, InterruptedException {
        if (buildConfiguration.local_debug) {
            log.info("updateImage {} is true,skip build image", ConfigKey.LOCAL_DEBUG_KEY);
        } else {
            String imageName = buildConfiguration.getPushImageName();
            boolean imageExist = dockerExtendClient.isImageExist(imageName);
            if (buildConfiguration.docker_force_push || !imageExist) {
                createJarIfAbsent();
                writeDockerFile();
                buildAndPushAndCleanImage();
            }
        }

    }

    private void buildAndPushAndCleanImage() throws InterruptedException {
        HashSet<String> tags = new HashSet<>();
        tags.add(buildConfiguration.getPushImageName());
        File dockerFile = new File(buildConfiguration.getTargetDockerFilePath());
        String imageId = dockerExtendClient.dockerClient.buildImageCmd()
                .withDockerfile(dockerFile)
                .withBaseDirectory(dockerFile.getParentFile())
                .withTags(tags)
                .exec(new BuildImageResultCallback())
                .awaitImageId();
        log.info("buildImage buildImageCmd success imageId {}", imageId);
        String imageNameWithRepo = buildConfiguration.getImageNameWithRepo();
        String imageTag = buildConfiguration.getImageTag();
        dockerExtendClient.dockerClient.tagImageCmd(imageId,imageNameWithRepo,imageTag);
        dockerExtendClient.dockerClient.pushImageCmd(imageNameWithRepo)
                .withTag(imageTag)
                .start().awaitCompletion();
        log.info("pushImage pushImageCmd success imageNameWithRepo {} imageTag {}", imageNameWithRepo, imageTag);
        dockerExtendClient.dockerClient.removeImageCmd(imageNameWithRepo)
                .withForce(true)
                .withNoPrune(true)
                .withImageId(imageId)
                .exec();
        log.info("removeImage removeImageCmd success imageNameWithRepo {} imageTag {}", imageNameWithRepo, imageTag);
    }

    public void writeDockerFile() throws IOException {
        String dockerFileContent = FileExtendUtils.readCharacterFileToUtf8Str(buildConfiguration.docker_file_temp, true);
        String finalName = getProject().getBuild().getFinalName();
        String copyJarCommand = String.format(ConfigKey.DOCKER_EXAMPLE_COPY_TEMP, finalName);
        dockerFileContent = dockerFileContent.replace(ConfigKey.DOCKER_EXAMPLE_COPY_KEY, copyJarCommand);
        FileExtendUtils.write2File(buildConfiguration.getTargetDockerFilePath(), dockerFileContent);
    }

    private void updateDnsConfigMap() {
        if (buildConfiguration.deploy_dns_update_enable && isStatefulService()) {
            try {
                V1ConfigMap dnsConfigMap = k8sExtendClient.coreV1Api.readNamespacedConfigMap("coredns", "kube-system", null, true, null);
                String homoDomains = dnsConfigMap.getData().get("customdomains");
                homoDomains = normalizeHostContent(homoDomains);
                log.debug("updateDnsConfigMap load homoDomains {}", homoDomains);
                String buildServiceFilePath = buildConfiguration.getCloudServiceBuildFile();
                List<KubernetesType> kubeObjs = FileExtendUtils.readFileToK8sObjs(buildServiceFilePath, false);
                String statefulService = "";
                for (KubernetesType kubeObj : kubeObjs) {
                    V1Service service = (V1Service) kubeObj;
                    Map<String, String> labels = service.getMetadata().getLabels();
                    if (labels != null && labels.containsKey(ConfigKey.STATEFUL_LABELS)) {
                        statefulService = labels.get(ConfigKey.STATEFUL_LABELS);
                        break;
                    }
                }
                String service0Domain = buildConfiguration.getStatefulService0Domain(statefulService);
                String dnsEntry = buildConfiguration.getLocal_ip() + " " + service0Domain;
                if (buildConfiguration.getLocal_debug()) {
                    log.info("updateDnsConfigMap add dnsEntry {}", dnsEntry);
                    homoDomains = addALine(homoDomains, dnsEntry, service0Domain);
                } else {
                    log.info("updateDnsConfigMap remove dnsEntry {}", dnsEntry);
                    homoDomains = removeALine(homoDomains, dnsEntry, service0Domain);
                }
                homoDomains = normalizeHostContent(homoDomains);
                dnsConfigMap.getData().put("customdomains", homoDomains);
                log.debug("updateDnsConfigMap update homoDomains {}", homoDomains);
//                k8sExtendClient.updateConfigMap("kube-system", dnsConfigMap);
            } catch (Exception e) {
                log.error("updateDnsConfigMap error", e);
            }
        }
    }

    private String addALine(String content, String subStr, String domain) {
        if (content == null || content.isEmpty()) {
            getLog().debug("content empty, just add this entry");
            return subStr;
        }
        if (content.contains(domain)) {
            //已有此域名,替换这条映射
            //这个正则想要匹配包含domain的那行host映射,但其实domain里的"."会被视为正则符号"."从而匹配任意字符,不过应该没有问题
            String regx = ".*" + domain;
            return content.replaceAll(regx, subStr);
        }

        getLog().debug("entry not found, add it");
        return content
                + "\n"
                + subStr;
    }

    private String removeALine(String content, String subStr, String domain) {
        if (content == null || content.isEmpty()) {
            getLog().debug("content empty, return empty");
            return "";
        }
//        String oldStr = subStr + "\n";
        String regx = ".*" + domain;
        return content.replaceAll(regx, "");
    }

    /**
     * 格式化host 内容, 读取后写入前各调用一次
     * note: 保证最后一行(非空情况下)有换行兼容老版本(1.0.0.14之前)
     */
    private String normalizeHostContent(String hostContent) {
        if (hostContent == null) {
            hostContent = "";
        }
        //去除中间多余的换行符和首尾的空白格(包括space,换行符和制表符等)
        return hostContent.replaceAll("\n\n*", "\n").trim() + "\n";
    }

    private void updateApolloConfigMap() throws ApiException {
        V1ConfigMap configMap = new V1ConfigMap();
        Map<String, String> data = new HashMap<>();
        String k8sNamespace = buildConfiguration.getK8s_namespace();
        StringBuilder configDataBuilder = new StringBuilder()
                .append("apollo.meta=").append(buildConfiguration.apollo_addr).append("\n")
                .append("env=").append(buildConfiguration.apollo_env).append("\n")
                .append("dic=").append(buildConfiguration.apollo_idc).append("\n");
        data.put("server.properties", configDataBuilder.toString());
        V1ObjectMeta meta = new V1ObjectMeta();
        meta.setName("apollo-config");
        meta.setNamespace(k8sNamespace);
        configMap.setMetadata(meta);
        configMap.setData(data);
        log.info("updateApolloConfigMap server.properties {}", configDataBuilder);
        k8sExtendClient.updateConfigMap(k8sNamespace, configMap);
    }

    private void checkAndCreatePvc() throws IOException, ApiException {
        if (buildConfiguration.deploy_filesystem_pvc_enable) {
            V1PersistentVolumeClaim pvc = buildConfiguration.createPvcTemp();
            String k8sNamespace = buildConfiguration.getK8s_namespace();
            String fileSelect = String.format("metadata.name=%s", pvc.getMetadata().getName());
            V1PersistentVolumeClaimList pvcList = k8sExtendClient.coreV1Api.listNamespacedPersistentVolumeClaim(k8sNamespace, null, false, null, fileSelect, null, 1, null, null, false);
            if (pvcList == null || pvcList.getItems().size() == 0) {
                log.info("checkAndCreatePvc create k8sNamespace {} pvc {}", k8sNamespace, pvc);
                FileExtendUtils.writeK8sObjToFile(buildConfiguration.getPvcFileName(), pvc);
                k8sExtendClient.coreV1Api.createNamespacedPersistentVolumeClaim(k8sNamespace, pvc, null, null, null);
            } else {
                log.info("checkAndCreatePvc k8sNamespace {} pvc {} is exist", k8sNamespace, pvc);
            }
        }
    }

    private void checkAndCreateK8sNamespace() throws ApiException {
        k8sExtendClient.createNamespaceIfAbsent(buildConfiguration.getK8s_namespace());
    }

    private void updateApollo() throws IOException {
        if (buildConfiguration.deploy_apollo_update_enable) {
            Map<String, Properties> propertiesMap = FileExtendUtils.readPropertiesFiles("devops/apollo");
            String apolloUpdateStrategy = buildConfiguration.getDeploy_apollo_update_strategy();
            for (Properties properties : propertiesMap.values()) {
                String appId = properties.getProperty(ConfigKey.APOLLO_APP_ID);
                String cluster = properties.getProperty(ConfigKey.APOLLO_CLUSTER);
                String namespace = properties.getProperty(ConfigKey.APOLLO_NAMESPACE);
                boolean isPublic = Boolean.parseBoolean((String) properties.getOrDefault(ConfigKey.APOLLO_NAMESPACE_PUBLIC, "false"));
                Map<String, String> propertyMap = new HashMap<>();
                for (String key : properties.stringPropertyNames()) {
                    if (!ConfigKey.APOLLO_APP_ID.equals(key) && !ConfigKey.APOLLO_CLUSTER.equals(key) &&
                            !ConfigKey.APOLLO_NAMESPACE.equals(key) && !ConfigKey.APOLLO_NAMESPACE_PUBLIC.equals(key)) {
                        propertyMap.put(key, properties.getProperty(key));
                    }
                }
                if (ConfigKey.APOLLO_UPDATE_STRATEGY_VALUE_SET.equals(apolloUpdateStrategy)) {
                    apolloExtendClient.createOrUpdateNamespaceCoverValue(appId, buildConfiguration.apollo_env, cluster, buildConfiguration.getApollo_editor(), isPublic, namespace, propertyMap);
                } else if (ConfigKey.APOLLO_UPDATE_STRATEGY_VALUE_SET_ABSENT.equals(apolloUpdateStrategy)) {
                    apolloExtendClient.createOrUpdateNamespaceUpdateIfAbsent(appId, buildConfiguration.apollo_env, cluster, buildConfiguration.getApollo_editor(), isPublic, namespace, propertyMap);
                } else {
                    log.info("updateApollo apolloUpdateStrategy is error!");
                }
            }
        } else {
            log.info("updateApollo updateApollo close!");
        }
    }

    public void createJarIfAbsent() throws IOException {
        MavenProject mavenProject = getProject();
        String targetDirectory = mavenProject.getBuild().getDirectory();//target目录
        String jarName = mavenProject.getBuild().getFinalName() + ".jar";
        File jarFile = new File(targetDirectory, jarName);
        boolean jarFileExist = jarFile.exists();
        if (jarFileExist) {
            log.info("createJarIfAbsent jarFile {} is exist,skip this process", jarFile);
        } else {
            log.info("createJarIfAbsent jarFile {} is absent,create it", jarFile);
            File projectPath = FileExtendUtils.getProjectRootDir(getProject().getBasedir());
            List<String> execCommand = new LinkedList<>();
            List<String> execResult = new LinkedList<>();
            execCommand.add("mvn clean package -Dmaven.test.skip=true -Dhomo-maven-plugin.skip=true -U -f " + projectPath.getAbsolutePath());
            boolean execResultFlag = CommandUtils.executeCommand(execCommand, execResult);
            if (!execResultFlag) {
                log.error("createJarIfAbsent execCommand {} execResult {} error", execCommand, execResult);
                String error = String.format("createJarIfAbsent projectPath %s execCommand %s execResult %s error", projectPath, execCommand, execResult);
                throw new RuntimeException(error);
            }
            log.info("createJarIfAbsent success");
        }
    }

    private void initConfig() throws IOException {
        buildConfiguration = BuildConfiguration.getInstance();
        buildConfiguration.init(this);
        k8sExtendClient = new K8sExtendClient(buildConfiguration.getK8sConfig());
        apolloExtendClient = new ApolloExtendClient(buildConfiguration.getApollo_addr(), buildConfiguration.getApollo_token());
        dockerExtendClient = new DockerExtendClient(buildConfiguration.getDocker_repo_username(), buildConfiguration.getDocker_repo_password());
    }
}
