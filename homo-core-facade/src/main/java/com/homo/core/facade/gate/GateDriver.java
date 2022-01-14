package com.homo.core.facade.gate;

import com.homo.core.common.faccade.Driver;
import com.homo.core.utils.callback.CallBack;

/**
 * tcp服务器驱动
 */
public interface GateDriver extends Driver {
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

    /**
     * 消息内部寻址转发
     * @param msgType
     * @param msg
     * @param callBack
     */
    void dispatcher(String msgType, byte[] msg, CallBack callBack);
}
