package com.homo.core.common.module;

public interface RootModule extends Module{

    default Integer getOrder() {
        return Integer.MIN_VALUE ;
    }

    ServerInfo defaultServerInfo = new ServerInfo();

    String getPodName();
}
