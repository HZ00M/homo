package com.homo.core.facade.gate;

import com.homo.core.utils.rector.Homo;

/**
 * 网关驱动  提供双向连接的能力
 */
public interface GateDriver<T> {
    /**
     * 开启一个网关驱动
     *
     * @param gateServer 网关服务对象
     */
    void startGate(GateServer<T> gateServer);

    /**
     * 关闭一个网关驱动
     */
    void closeGate() throws Exception;

    /**
     * 关闭一个网关客户端
     *
     * @param gateClient 网关客户端对象
     */
    void closeGateClient(GateClient<T> gateClient);

    /**
     * 发送一条消息到客户端
     *
     * @param gateClient 网关客户端对象
     * @param msg        消息内容
     */
    Homo<Boolean> pong(GateClient<T> gateClient, T msg);

    /**
     * 广播一条消息到所有客户端
     *
     * @param msg        消息内容
     */
    Homo<Boolean> broadcast( T msg);
}
