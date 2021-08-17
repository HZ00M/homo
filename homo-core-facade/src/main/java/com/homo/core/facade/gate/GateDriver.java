package com.homo.core.facade.gate;

/**
 * tcp服务器驱动
 */
public interface GateDriver {
    /**
     * 开启一个服务器
     * @param gateServer
     */
    void startGateServer(GateServer gateServer);

    /**
     * 关闭一个服务器
     * @param gateServer
     */
    void closeGateServer(GateServer gateServer);

    /**
     * 关闭一个客户端
     * @param gateClient
     */
    void closeGateClient(GateClient gateClient);

    /**
     * 发送一条消息到客户端
     * @param gateClient
     * @param msgType
     * @param msg
     */
    void sendToClient(GateClient gateClient,String msgType,byte[] msg);
}
