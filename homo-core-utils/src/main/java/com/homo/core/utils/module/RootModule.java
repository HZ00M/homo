package com.homo.core.utils.module;

public interface RootModule extends Module{

    default Integer getOrder() {
        return Integer.MIN_VALUE ;
    }

    ServerInfo defaultServerInfo = new ServerInfo();

    String getPodName();
    /**
     * 获取服务信息
     *
     * @return
     */
    ServerInfo getServerInfo();
}
