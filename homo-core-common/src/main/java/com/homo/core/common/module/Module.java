package com.homo.core.common.module;


import java.util.HashMap;
import java.util.Map;

/**
 * 模块组件  所有组件都继承
 */
public interface Module {
    Map<ModuleInfoType, Object> moduleInfo = new HashMap<>();

    /**
     * 返回模块的初始化顺序，值越小，越早初始化  RootModule> SupportModule >DriverModule >ServiceModule >Module
     *
     * @return 初始化顺序值
     */
    default Integer getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * 获取服务信息
     *
     * @return
     */
    default ServerInfo getServerInfo() {
        return getInfo(ModuleInfoType.SERVER_INFO, ServerInfo.class);
    }

    /**
     * 获取服务信息
     *
     * @return
     */
    default <T> T getInfo(ModuleInfoType moduleInfoType, Class<T> type) {
        return (T) moduleInfo.getOrDefault(moduleInfoType, null);
    }

    /**
     * 模块初始化
     */
    default void init(RootModule rootModule) {
        moduleInfo.put(ModuleInfoType.SERVER_INFO, rootModule.getServerInfo());
    }

    /**
     * 模块初始化
     */
    default void init() {
    }

    /**
     * 所有模块初始化完毕调用
     */
    default void afterAllModuleInit() {
    }

    /**
     * 初始化时给子类的回调
     */
    default void preInit() {

    }

    /**
     * 初始化时给子类的回调
     */
    default void postInit() {
    }

    /**
     * 服务关闭时调用
     */
    default void onStop() {
    }
}
