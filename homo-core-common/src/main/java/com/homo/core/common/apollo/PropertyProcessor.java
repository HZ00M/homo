package com.homo.core.common.apollo;

import com.google.common.base.Strings;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

;

/**
 * 配置中心属性读取
 */
@Log4j2
public class PropertyProcessor {
    private static final String CONFIG_PROPERTIES_WINDOWS = "C:/opt/settings/server.properties";
    private static final String SERVER_PROPERTIES_LINUX = "/opt/settings/server.properties";
    public static final String CONFIG_APP_PROPERTIES = "/META-INF/app.properties";
    public static final String CONFIG_APPLICATION_PROPERTIES = "/application.properties";

    private final Properties serverProperties = new Properties();
    @Getter
    String meta,env,idc;

    private final Properties appProperties = new Properties();
    @Getter
    String appId;

    private final Properties applicationProperties = new Properties();
    @Getter
    String[] namespaces;

    private static PropertyProcessor processor;
    private PropertyProcessor(){
    }
    public static PropertyProcessor getInstance(){
        if(processor == null){
            processor = new PropertyProcessor();
            processor.init();
        }
        return processor;
    }

    private void init(){
        String osName = Strings.nullToEmpty(System.getProperty("os.name"));
        String path = osName.startsWith("Windows")?CONFIG_PROPERTIES_WINDOWS:SERVER_PROPERTIES_LINUX;
        log.info("config path: {}", path);
        try(FileInputStream envFis = new FileInputStream(path)){
            InputStream appFis = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_APP_PROPERTIES);
            InputStream applicationFis = Thread.currentThread().getContextClassLoader().getResourceAsStream(CONFIG_APPLICATION_PROPERTIES);
            if(appFis == null){
                appFis = PropertyProcessor.class.getResourceAsStream(CONFIG_APP_PROPERTIES);
            }
            if(applicationFis == null){
                applicationFis = PropertyProcessor.class.getResourceAsStream(CONFIG_APPLICATION_PROPERTIES);
            }
            log.info("loading config env");

            serverProperties.load(envFis);
            meta = serverProperties.getProperty("apollo.meta");
            env = serverProperties.getProperty("env");
            idc = serverProperties.getProperty("idc");

            try {
                appProperties.load(appFis);
            }finally {
                appFis.close();
            }
            appId = appProperties.getProperty("app.id");

            try {
                applicationProperties.load(applicationFis);
            }finally {
                applicationFis.close();
            }

            namespaces = applicationProperties.getProperty("apollo.bootstrap.namespaces").split(",");

        }catch (Exception e){
            log.error("config read has exception! system quit ", e);
        }
    }
}
