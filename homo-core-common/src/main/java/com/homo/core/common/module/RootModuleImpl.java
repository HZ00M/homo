package com.homo.core.common.module;

import com.ctrip.framework.apollo.Config;
import com.homo.core.common.apollo.ConfigDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public class RootModuleImpl implements RootModule{
    public static String ROOT_INFO_NAMESPACE = "homo_root_info";
    public static String SERVER_APP_ID = "server.info.appId";
    public static String SERVER_REGION_ID = "server.info.regionId";
    public static String SERVER_NAMESPACE = "server.info.namespace";
    public static String SERVER_CHANNEL = "server.info.channel";
    public static String SERVER_SERVERNAME = "server.info.serverName";
    public static String SERVER_IS_TEST_ENV = "server.info.isTestEnv";
    public static String SERVER_POD_NAME = "POD_NAME";
    @Autowired(required = false)
    public ConfigDriver configDriver;
    /** 是否运行在测试环境 */
    private boolean isInTestEnv = false;
    private String podName;

    @Override
    public void init() {
        Config config = configDriver.registerNamespace(ROOT_INFO_NAMESPACE);
        defaultServerInfo.appId = config.getProperty(SERVER_APP_ID,"1");
        defaultServerInfo.regionId = config.getProperty(SERVER_REGION_ID,"1");
        defaultServerInfo.namespace = config.getProperty(SERVER_NAMESPACE,"1");
        defaultServerInfo.channel = config.getProperty(SERVER_CHANNEL,"*");
        defaultServerInfo.serverName = config.getProperty(SERVER_SERVERNAME,"null");
        log.info("RootModule init defaultServerInfo {}",defaultServerInfo);
        configDriver.listenerKey(SERVER_IS_TEST_ENV,newValue->isInTestEnv = Boolean.getBoolean(newValue));
        podName = System.getenv(SERVER_POD_NAME);
        if (this.podName == null) {
            log.error("RootModule init can not get podName in env!");
            this.podName = "for-local-debug-0";
        }
        log.info("RootModule init isInTestEnv {} podName {}",isInTestEnv,podName);
    }

    @Override
    public ServerInfo getServerInfo() {
        return defaultServerInfo;
    }

    @Override
    public String getPodName() {
        return podName;
    }
}
