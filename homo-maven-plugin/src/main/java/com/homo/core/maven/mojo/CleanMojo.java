package com.homo.core.maven.mojo;

import com.homo.core.exend.utils.FileExtendUtils;
import com.homo.core.maven.BuildConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

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
            buildConfiguration = BuildConfiguration.getInstance();
            List<String> deleteFileList = new ArrayList<>();
            //build文件
            deleteFileList.add(buildConfiguration.getDeployment_build_temp_yaml());
            deleteFileList.add(buildConfiguration.getStatefulSet_build_temp_yaml());
            deleteFileList.add(buildConfiguration.getLocal_service_build_temp_yaml());
            deleteFileList.add(buildConfiguration.getCloud_service_build_temp_yaml());
            FileExtendUtils.deleteFiles(deleteFileList.toArray(new String[0]));
            log.info("homoClean clean finish deleteFileList {}", deleteFileList);
        } catch (Exception e) {
            log.error("homoClean clean error", e);
        }
    }
}
