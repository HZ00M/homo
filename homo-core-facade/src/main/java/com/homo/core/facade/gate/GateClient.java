package com.homo.core.facade.gate;

import com.homo.core.utils.rector.Homo;

/**
 * 网关客户端对象  提供发送消息到指定服务器的能力
 */
public interface GateClient {

    String name();

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
    GateServer getGateServer();

    Homo<Boolean> sendToClient(String msgId, byte[] msg) ;

    Homo<Boolean> sendToClient(String msgId, byte[] msg, Short sessionId, Short clientSendSeq, Short confirmServerSendSeq) ;
    Homo<Boolean> sendToClientComplete(String msgId, byte[] msg) ;
    Homo<Boolean> sendToClientComplete(String msgId, byte[] msg, Short sessionId, Short clientSendSeq, Short confirmServerSendSeq);

    void close();


}
