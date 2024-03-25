package com.homo.core.utils.module;

import com.homo.core.utils.apollo.ConfigDriver;
import com.homo.core.configurable.module.ModuleProperties;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.module.ServerInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

@Slf4j
public class RootModuleImpl implements RootModule {
    @Autowired(required = false)
    ModuleProperties moduleProperties;
    public static String SERVER_POD_NAME = "POD_NAME";
    @Autowired(required = false)
    public ConfigDriver configDriver;
    /** 是否运行在测试环境 */
    private boolean isInTestEnv;
    private String podName;

    @Override
    public void moduleInit() {
        defaultServerInfo.appId = moduleProperties.getAppId();
        defaultServerInfo.regionId = moduleProperties.getRegionId();
        defaultServerInfo.namespace = moduleProperties.getNamespace();
        defaultServerInfo.channel = moduleProperties.getChannel();
        defaultServerInfo.serverName = "default";
        if (StringUtils.isEmpty(defaultServerInfo.serverName)) {
            defaultServerInfo.serverName = moduleProperties.getServerName();
            log.error("RootModule init can not get serverName!");
             System.exit(-1);
        }
        log.info("RootModule init defaultServerInfo {}",defaultServerInfo);
        isInTestEnv = moduleProperties.getIsTestEnv();
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
