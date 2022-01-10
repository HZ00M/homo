package com.homo.core.facade.module;

/**
 * 模块组件  所有组件都继承
 */
public interface Module {

    /**
     * 保存服务信息，由ModuleMgr调用
     * @param serverInfo
     */
    void setServerInfo(ServerInfo serverInfo);

    /**
     * 获取服务信息
     * @return
     */
    ServerInfo getServerInfo();

    /**
     * 模块初始化
     */
    default void init(){}

    /**
     * 所有模块初始化完毕调用
     */
    default void afterAllModuleInit(){}
}
