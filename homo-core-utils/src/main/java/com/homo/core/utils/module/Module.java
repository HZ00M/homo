package com.homo.core.utils.module;


/**
 * 模块组件  所有组件都继承
 */
public interface Module {
    /**
     * 返回模块的初始化顺序，值越小，越早初始化  RootModule> SupportModule >DriverModule >ServiceModule >Module
     *
     * @return 初始化顺序值
     */
    default Integer getOrder() {
        return Integer.MAX_VALUE;
    }

    /**
     * 模块初始化
     */
    default void moduleInit() {
    }

    /**
     * 所有模块初始化完毕调用
     */
    default void afterAllModuleInit() {
    }
    /**
     * 服务关闭时调用
     */
    default void beforeClose() {
    }
}
