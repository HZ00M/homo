package com.homo.core.maven.apollo;

import com.ctrip.framework.foundation.internals.provider.DefaultServerProvider;
import com.homo.core.maven.ConfigKey;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;

@Slf4j
public class CustomServerProvider extends DefaultServerProvider {
    public static final String SERVER_PROPERTIES_WINDOWS = "C:/opt/settings/server.properties";
    public static final String SERVER_PROPERTIES_FILE_NAME = "server.properties";
    private static CustomServerProvider instance;

    public static CustomServerProvider getInstance() {
        return instance;
    }

    @Override
    public void initialize() {
        try {
            instance = this;
            String serverPropertyPath = getServerPropertyPath();
            File file = new File(serverPropertyPath);
            if (file.exists() && file.canRead()) {
                log.info("initialize Loading {}", file.getAbsolutePath());
                FileInputStream fis = new FileInputStream(file);
                initialize(fis);
                return;
            }
            initialize(null);
        } catch (Exception e) {
            log.info("Initialize CustomServerProvider failed.", e);
        }
    }

    private String getServerPropertyPath() {
        String projectPath = System.getProperty(ConfigKey.PROJECT_BASE_DIR);
        if (projectPath == null) {
            log.info("getServerPropertyPath server.properties from file {} sys custom path does not exist!", SERVER_PROPERTIES_WINDOWS);
            return SERVER_PROPERTIES_WINDOWS;
        }
        String customServerFile = projectPath + File.separator + ConfigKey.PROJECT_GIT_IGNORE_DEVOPS_DIR_NAME + File.separator + SERVER_PROPERTIES_FILE_NAME;
        File file = new File(customServerFile);
        if (!file.exists() || !file.canRead()) {
            log.info("getServerPropertyPath server.properties from  file {} doesn't exist or can not read", customServerFile);
            return SERVER_PROPERTIES_WINDOWS;
        }
        log.info("getServerPropertyPath server.properties from  file {} use to custom apollo", customServerFile);
        return customServerFile;
    }
}
