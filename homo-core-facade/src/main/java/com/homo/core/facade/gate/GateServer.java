package com.homo.core.facade.gate;

import com.homo.core.utils.rector.Homo;

/**
 *网关服务接口 提供通知客户端能力
 */
public interface GateServer {
    /**
     * 新网关客户端接入的回调
     * @param addr  网关客户端ip地址
     * @param port  网关客户端端口
     * @return  返回网关客户端连接对象
     */
    GateClient newClient(String addr,int port);

    /**
     * 获取服务名
     * @return  服务名
     */
    String getName();

    /**
     * 获取监听端口
     * @return  监听端口
     */
    int getPort();

    /**
     * 服务器通知客户端
     * @param gateClient 网关客户端对象
     * @param msg 消息内容
     * @return
     */
    Homo<Boolean> sendToClient(GateClient gateClient,String msgId, byte[] msg, short sessionId, short sendSeq, short recvSeq);

    Homo<Boolean> sendToClient(GateClient gateClient,String msgId, byte[] msg);

    Homo<Boolean> sendToClientComplete(GateClient gateClient,String msgId, byte[] msg);

    Homo<Boolean> sendToClientComplete(GateClient gateClient,String msgId, byte[] msg, short sessionId, short sendSeq, short recvSeq);

    /**
     * 服务器广播消息到客户端
     * @param data 消息内容
     * @return
     */
    void broadcast(String msgType,byte[] data);

    /**
     * 获取驱动
     * @return
     */
    GateDriver getDriver();

    void setDriver(GateDriver gateDriver);

    void closeClient(GateClient gateClient);

}
