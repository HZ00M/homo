package com.homo.core.common.module;

public interface RootModule {
    ServerInfo defaultServerInfo = new ServerInfo();

    void init();

    default ServerInfo getServerInfo(){
        return defaultServerInfo;
    }

    default String getAppId(){
        return getServerInfo().getAppId();
    }

    default String getRegionId(){
        return getServerInfo().getRegionId();
    }

    String getPodName();
}
