package com.homo.core.maven.mojo;

import com.homo.core.exend.utils.FileExtendUtils;
import com.homo.core.maven.HomoServiceSetter;
import com.homo.core.maven.HomoServiceSetterFactory;
import com.homo.core.maven.BuildConfiguration;
import io.kubernetes.client.common.KubernetesType;
import io.kubernetes.client.openapi.models.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.artifact.DependencyResolutionRequiredException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * name = "generate"：指定了Mojo的名称为"generate"。
 * <plugin>
 * <groupId>com.homo</groupId>
 * <artifactId>homo-maven-plugin</artifactId>
 * <executions>
 * <execution>
 * <goals>
 * <goal>generate</goal>  //名字在这里会使用到  当maven生命周期在该阶段自动执行该mojo
 * </goals>
 * </execution>
 * </executions>
 * </plugin>
 * defaultPhase = LifecyclePhase.PACKAGE：指定了Mojo默认执行的阶段是"Maven生命周期中的PACKAGE阶段"。也就是说，在执行Maven构建过程时，该插件会在PACKAGE阶段触发。
 * requiresDependencyResolution = ResolutionScope.COMPILE：指定了该插件需要依赖解析范围为COMPILE级别。
 */
@Mojo(name = "homoBuild",
        defaultPhase = LifecyclePhase.PACKAGE,
        requiresDependencyCollection = ResolutionScope.COMPILE)
@Slf4j
public class BuildMojo extends AbsHomoMojo<BuildMojo> {
    private V1Deployment deployment;
    private V1StatefulSet statefulSet;
    private BuildConfiguration buildConfiguration;

    @Override
    protected void doExecute() throws MojoFailureException {
        try {
            //初始化配置
            initConfig();
            //前提条件检查
            checkPrerequisite();
            //加载服务类
            loadServices();
            ///初始化模板
            propreBuildTemp();
            //生成应用实例部署文件
            generateAppDeployFile();
            //生成service文件
            generateServiceFile();
        } catch (Exception e) {
            throw new MojoFailureException("homoClean generate fail!", e);
        }
    }



    public void initConfig() throws MojoFailureException {
        buildConfiguration = BuildConfiguration.getInstance();
        buildConfiguration.init(this);
        HomoServiceSetterFactory.init(this);
        log.info("homoClean init finish buildConfiguration {}",buildConfiguration);
    }

    public void generateAppDeployFile() throws IOException {
        //生成模块配置 1、添加框架标签 2、添加环境变量
        if (HomoServiceSetterFactory.isStatefulService()) {
            V1Container container = statefulSet.getSpec().getTemplate().getSpec().getContainers().get(0);
            BuildConfiguration.appendDeploymentLabelsInfo(statefulSet);
            //有状态服务器 1将服务名改成有状态服务的名称
            statefulSet.getMetadata().setName(HomoServiceSetterFactory.mainServiceSetter.getServiceName());
            statefulSet.getSpec().setServiceName(HomoServiceSetterFactory.mainServiceSetter.getServiceName());
            BuildConfiguration.appendContainerEnvInfo(container);
            //生成服务配置  1 暴露服务端口
            BuildConfiguration.appendContainerExportPortInfo(container);
            Path writeFilePath = Paths.get(project.getBasedir().toString(), buildConfiguration.getStatefulSet_build_temp_yaml());
            FileExtendUtils.writeK8sObjToFile(writeFilePath.toString(), statefulSet);
            log.info("homoBuild statefulSet file {} success path {} ", buildConfiguration.getStatefulSet_build_temp_yaml(), writeFilePath);
        } else {
            V1Container container = deployment.getSpec().getTemplate().getSpec().getContainers().get(0);
            BuildConfiguration.appendDeploymentLabelsInfo(deployment);
            BuildConfiguration.appendContainerEnvInfo(container);
            //生成服务配置  1 暴露服务端口
            BuildConfiguration.appendContainerExportPortInfo(container);
            Path writeFilePath = Paths.get(project.getBasedir().toString(), buildConfiguration.getDeployment_build_temp_yaml());
            FileExtendUtils.writeK8sObjToFile(writeFilePath.toString(), deployment);
            log.info("homoBuild deployment file {} success path {} ", buildConfiguration.getDeployment_build_temp_yaml(), writeFilePath);
        }
//        FileExtendUtils.saveStringFile(new File(getCompleteFilePath()),"complete");
    }

    private void generateServiceFile() throws IOException {
        Collection<HomoServiceSetter> setters = HomoServiceSetterFactory.setterMap.values();
        if (setters.size() > 0) {
            List<KubernetesType> localService = new ArrayList<>();
            List<KubernetesType> cloudService = new ArrayList<>();
            for (HomoServiceSetter setter : setters) {
                if (!setter.isStateful()){
                    V1Service clusterServiceTemp = buildConfiguration.createClusterServiceTemp(setter);
                    V1Endpoints endPointsTemp = buildConfiguration.createEndPointsTemp(setter);
                    localService.add(clusterServiceTemp);
                    localService.add(endPointsTemp);
                    log.info("homoBuild localService file {} success path {} ", buildConfiguration.getLocal_service_build_temp_yaml(), clusterServiceTemp);
                    log.info("homoBuild endPointsTemp file {} success path {} ", buildConfiguration.getLocal_service_build_temp_yaml(), endPointsTemp);

                }
                //cloud
                V1Service headlessServiceTemp = BuildConfiguration.createHeadlessServiceTemp(setter);
                cloudService.add(headlessServiceTemp);
                log.info("homoBuild cloudService file {} success path {} ", buildConfiguration.getCloud_service_build_temp_yaml(), headlessServiceTemp);
            }
            if (localService.size() > 0) {
                Path writeClusterFilePath = Paths.get(project.getBasedir().toString(), buildConfiguration.getLocal_service_build_temp_yaml());
                FileExtendUtils.writeYamlObjToFile(writeClusterFilePath.toString(),localService.iterator());
            }
            if (cloudService.size() > 0) {
                Path writeHeadlessFilePath = Paths.get(project.getBasedir().toString(), buildConfiguration.getCloud_service_build_temp_yaml());
                FileExtendUtils.writeYamlObjToFile(writeHeadlessFilePath.toString(),cloudService.iterator());
            }
        }
    }

    public void propreBuildTemp() throws IOException {
        deployment = buildConfiguration.createDeploymentTemp();
        statefulSet = buildConfiguration.createStatefulSetTemp();
    }

    public void checkPrerequisite() throws MojoFailureException {
        checkDirName();
    }

    public void loadServices() throws DependencyResolutionRequiredException, ClassNotFoundException, IOException {
        HomoServiceSetterFactory.loadServices();
    }
}
