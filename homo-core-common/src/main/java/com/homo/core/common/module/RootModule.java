package com.homo.core.common.module;

public interface RootModule {
    ServerInfo defaultServerInfo = new ServerInfo();

    void init();

    default ServerInfo getServerInfo(){
        return defaultServerInfo;
    }

    String getPodName();
}
