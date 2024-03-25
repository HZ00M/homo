package com.homo.core.utils.apollo;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Stream;

;

/**
 * 配置中心属性读取
 */
@Slf4j
public class PropertyProcessor {
    private static final String CONFIG_PROPERTIES_WINDOWS = "C:/opt/settings/server.properties";
    private static final String SERVER_PROPERTIES_LINUX = "/opt/settings/server.properties";
    public static final String CONFIG_APP_PROPERTIES = "/META-INF/app.properties";
    public static final String CONFIG_APPLICATION_PROPERTIES = "/application.properties";

    private final Properties serverProperties = new Properties();
    @Getter
    String meta, env, idc;

    private final Properties appProperties = new Properties();
    @Getter
    String appId;

    private final Properties applicationProperties = new Properties();
    @Getter
    Set<String> namespaces;
    public static String customNamespace = "apollo.bootstrap.namespaces";
    public static String frameNamespace = "homo_zipkin_config";

    private static PropertyProcessor processor;

    private PropertyProcessor() {
    }

    public static PropertyProcessor getInstance() {
        if (processor == null) {
            processor = new PropertyProcessor();
            processor.init();
        }
        return processor;
    }

    private void init() {
        namespaces = new HashSet<>();
        String osName = Strings.nullToEmpty(System.getProperty("os.name"));
        String path = osName.startsWith("Windows") ? CONFIG_PROPERTIES_WINDOWS : SERVER_PROPERTIES_LINUX;
        log.info("config path: {}", path);
        try (FileInputStream envFis = new FileInputStream(path)) {
            InputStream appFis = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_APP_PROPERTIES);
            InputStream applicationFis = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_APPLICATION_PROPERTIES);
            if (appFis == null) {
                appFis = PropertyProcessor.class.getResourceAsStream(CONFIG_APP_PROPERTIES);
            }
            if (applicationFis == null) {
                applicationFis = PropertyProcessor.class.getResourceAsStream(CONFIG_APPLICATION_PROPERTIES);
            }
            log.info("loading config env");

            serverProperties.load(envFis);
            meta = serverProperties.getProperty("apollo.meta");
            env = serverProperties.getProperty("env");
            idc = serverProperties.getProperty("idc");

            try {
                appProperties.load(appFis);
            } finally {
                appFis.close();
            }
            appId = appProperties.getProperty("app.id");

            try {
                applicationProperties.load(applicationFis);
            } finally {
                applicationFis.close();
            }
            namespaces.addAll(Arrays.asList(frameNamespace.split(",")));
            namespaces.addAll(Arrays.asList(applicationProperties.getProperty(customNamespace).split(",")));
        } catch (Exception e) {
            log.error("config read has exception! system quit ", e);
        }
    }
}
