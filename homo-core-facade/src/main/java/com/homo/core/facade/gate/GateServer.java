package com.homo.core.facade.gate;

import com.homo.core.utils.rector.Homo;

/**
 *网关服务接口 提供通知客户端能力
 */
public interface GateServer<T> {
    /**
     * 新网关客户端接入的回调
     * @param addr  网关客户端ip地址
     * @param port  网关客户端端口
     * @return  返回网关客户端连接对象
     */
    GateClient<T> newClient(String addr,int port);

    /**
     * 获取监听端口
     * @return  监听端口
     */
    int getPort();

    /**
     * 服务器通知客户端
     * @param gateClient 网关客户端对象
     * @param msgType  消息类型
     * @param data 消息内容
     * @return
     */
    <T,R> Homo<R> pong(GateClient<T> gateClient,String msgType,T data);

    /**
     * 服务器广播消息到网关客户端
     * @param data 消息内容
     * @return
     */
    <T,R> Homo<R> broadcast(T data);

    /**
     * 获取驱动
     * @return
     */
    GateDriver<T> getDriver();

}
