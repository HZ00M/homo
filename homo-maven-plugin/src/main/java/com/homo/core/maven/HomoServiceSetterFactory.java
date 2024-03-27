package com.homo.core.maven;

import com.homo.core.exend.utils.FileExtendUtils;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.maven.mojo.AbsHomoMojo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DependencyResolutionRequiredException;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.*;

@Slf4j
@Data
public class HomoServiceSetterFactory {
    public static AbsHomoMojo homoMojo;
    public static HomoServiceSetter mainServiceSetter;
    public static Map<String, HomoServiceSetter> setterMap = new HashMap<>();

    public static void init(AbsHomoMojo homoMojo) {
        HomoServiceSetterFactory.homoMojo = homoMojo;
    }

    public static HomoServiceSetter createIfExistService(Class clazz) {
        for (Class<?> clazzInterface : clazz.getInterfaces()) {
            ServiceExport serviceExport = clazzInterface.getAnnotation(ServiceExport.class);
            if (serviceExport != null) {
                HomoServiceSetter setter = HomoServiceSetter.builder()
                        .serviceClass(clazz)
                        .serviceExport(serviceExport)
                        .build();
                if (serviceExport.isMainServer()) {
                    mainServiceSetter = setter;
                }
                try {
                    setter.init();
                    setterMap.put(setter.getServiceName(), setter);
                    return setter;
                } catch (Exception e) {
                    String errMsg = String.format("%s parse service port error", serviceExport.tagName());
                    throw new RuntimeException(errMsg);
                }
            }
        }
        return null;
    }

    public static boolean isStatefulService() {
        return mainServiceSetter != null && mainServiceSetter.getServiceExport().isStateful();
    }

    public static void loadServices() throws IOException, DependencyResolutionRequiredException, ClassNotFoundException {
        Set<String> classNames = getProjectAllClassesName();
        ClassLoader classLoader = getBuildClassLoader(new HashSet<>(homoMojo.getProject().getCompileClasspathElements()));
        for (String className : classNames) {
            Class<?> clazz = classLoader.loadClass(className);
            HomoServiceSetter setter = HomoServiceSetterFactory.createIfExistService(clazz);
            if (setter != null) {
                log.info("load className {} service {}", className, setter);
            }
        }
    }

    private static Set<String> getProjectAllClassesName() throws IOException {
        BuildConfiguration buildConfiguration = BuildConfiguration.getInstance();
        Set<File> scanJars = new HashSet<>();
        Set<Artifact> artifacts = homoMojo.getProject().getArtifacts();
        for (Artifact artifact : artifacts) {
            /**
             * 扫描项目过滤，默认非使用homo框架的项目不扫描
             */
            if (!artifact.getGroupId().startsWith(buildConfiguration.getDeploy_scan_class_path())) {
                continue;
            }
            /**
             * 扫描范围，默认只扫描compile scope模块
             */
            if (!buildConfiguration.getDeploy_service_scan_scope().contains(artifact.getScope())) {
                continue;
            }
            /**
             * 非jar包不扫描
             */
            if (!artifact.getType().equals("jar")) {
                continue;
            }
            /**
             * 检查Jar包是否存在
             */
            File jarFile = artifact.getFile();
            if (!jarFile.exists()) {
                throw new RuntimeException("jar file not exist! " + jarFile.getAbsolutePath());
            }
            scanJars.add(jarFile);
        }
        Set<String> classNames = new HashSet<>();
        for (File jarFile : scanJars) {
            classNames.addAll(FileExtendUtils.getClassNameFromJarFile(jarFile));
        }
        /**
         * 扫描本模块
         */

        Set<String> classNameSet = FileExtendUtils.findAllClassNamesFromMavenProject(homoMojo.getProject());
        classNames.addAll(classNameSet);
        return classNames;
    }

    /**
     * 创建一个加载该项目及其依赖项目的类加载器
     */
    protected static ClassLoader getBuildClassLoader(Set<String> dirs) {
        try {
            URL urls[] = new URL[dirs.size()];
            int i = 0;
            for (String dir : dirs) {
                urls[i] = new File(dir).toURI().toURL();
                i++;
            }
            //自定义加载器
            return new URLClassLoader(urls, homoMojo.getClass().getClassLoader());
        } catch (Exception e) {
            log.debug("Couldn't get the classloader.");
            return homoMojo.getClass().getClassLoader();
        }
    }

    public static void clean(){
        setterMap.clear();
        mainServiceSetter = null;
    }

}
