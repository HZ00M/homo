package com.homo.core.common.module;

/**
 * 模块组件  所有组件都继承
 */
public interface Module {
    ServerInfo serverInfo = new ServerInfo() {
        @Override
        public String getAppId() {
            return "1";
        }

        @Override
        public String getRegionId() {
            return "1";
        }

        @Override
        public String getChannel() {
            return "*";
        }

        @Override
        public String getServiceName() {
            return "UNKNOWN_SERVICE";
        }
    };

    /**
     * 返回模块的初始化顺序，值越小，越早初始化
     * @return 初始化顺序值
     */
    default Integer getOrder(){
        return Integer.MAX_VALUE;
    }

    /**
     * 获取服务信息
     * @return
     */
    default ServerInfo getServerInfo(){
        return serverInfo;
    }

    /**
     * 模块初始化
     */
    default void init(){}

    /**
     * 所有模块初始化完毕调用
     */
    default void afterAllModuleInit(){}
}
