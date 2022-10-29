package com.homo.core.facade.gate;

/**
 * 网关客户端对象  提供发送消息到指定服务器的能力
 */
public interface GateClient<T> {

    /**
     * 连接打开回调函数
     */
    void onOpen();

    /**
     * 连接关闭回调函数
     * @param reason 关闭原因
     */
    void onClose(String reason);

    /**
     * 获取GateServer实例指针（与GateServer双向绑定）
     * @return GateServer实例指针
     */
    GateServer<T> getGateServer();

    /**
     * 客户端发送消息
     * @param data 消息
     * @return
     */
    <T> void ping(String targetService, String funName,T data);

}
