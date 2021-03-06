package com.homo.core.common.module;

import com.homo.core.common.apollo.ConfigDriver;
import com.homo.core.configurable.module.ModuleProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

@Log4j2
public class RootModuleImpl implements RootModule{
    public static String ROOT_INFO_NAMESPACE = "homo_root_info";
    @Autowired(required = false)
    ModuleProperties moduleProperties;
    public static String SERVER_POD_NAME = "POD_NAME";
    @Autowired(required = false)
    public ConfigDriver configDriver;
    /** 是否运行在测试环境 */
    private boolean isInTestEnv;
    private String podName;

    @Override
    public void init() {
        defaultServerInfo.appId = moduleProperties.getAppId();
        defaultServerInfo.regionId = moduleProperties.getRegionId();
        defaultServerInfo.namespace = moduleProperties.getNamespace();
        defaultServerInfo.channel = moduleProperties.getChannel();
        defaultServerInfo.serverName = "default";//todo 后期区分服务器
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
