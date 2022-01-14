package com.homo.core.facade.module;

/**
 * 模块组件  所有组件都继承
 */
public interface Module {

    /**
     * 返回模块的初始化顺序，值越小，越早初始化
     * @return 初始化顺序值
     */
    default Integer getOrder(){
        return Integer.MAX_VALUE;
    }

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
