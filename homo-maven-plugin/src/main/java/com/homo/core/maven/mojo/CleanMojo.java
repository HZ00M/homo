package com.homo.core.maven.mojo;

import com.homo.core.exend.utils.FileExtendUtils;
import com.homo.core.maven.BuildConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Mojo(name = "homoClean",
        defaultPhase = LifecyclePhase.CLEAN,
        requiresDependencyResolution = ResolutionScope.NONE)
public class CleanMojo extends AbsHomoMojo<CleanMojo> {
    private BuildConfiguration buildConfiguration;

    @Override
    protected void doExecute() throws MojoFailureException {
        try {
            initConfig();
            List<String> deleteFileList = new ArrayList<>();
            //build文件
            deleteFileList.add(buildConfiguration.getDeploymentBuildYaml());
            deleteFileList.add(buildConfiguration.getStatefulSetBuildYaml());
            deleteFileList.add(buildConfiguration.getLocalServiceBuildFile());
            deleteFileList.add(buildConfiguration.getLocalServiceFile());
            deleteFileList.add(buildConfiguration.getCloudServiceBuildFile());
            deleteFileList.add(buildConfiguration.getCloudServiceFile());
            //image文件
            deleteFileList.add(buildConfiguration.getTargetDockerFilePath());
            //deploy文件
            deleteFileList.add(buildConfiguration.getDeploymentFileName());
            deleteFileList.add(buildConfiguration.getStatefulSetFileName());
            FileExtendUtils.deleteFiles(deleteFileList.toArray(new String[0]));
            log.info("homoClean clean finish deleteFileList {}", deleteFileList);
//            deleteDirectory(new File(buildConfiguration.getDevopsRootDir()));
        } catch (Exception e) {
            log.error("homoClean clean error", e);
        }
    }


    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File file : files) {
                    deleteDirectory(file);
                }
            }
        }

        // 删除空目录或文件
        if (!directory.delete()) {
            log.error("delete file failed{} ", directory);
        } else {
            log.info("delete file success {} ", directory);
        }
    }

    public void initConfig() throws MojoFailureException {
        buildConfiguration = BuildConfiguration.getInstance();
        BuildConfiguration.homoMojo = this;
        ;
    }
}
