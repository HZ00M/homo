package com.homo.core.facade.gate;

/**
 * 客户端连接代理
 */
public interface GateServer {
    /**
     * 新客户端接入的回调
     * @param addr  客户端ip地址
     * @param port  客户端端口
     * @return  返回客户端连接对象
     */
    GateClient newClient(String addr,int port);

    /**
     * 获取监听端口
     * @return  监听端口
     */
    int getPort();

    /**
     * 获取监听地址
     * @return  返回监听地址
     */
    String getAddr();

    byte[] processError(String msgId, Throwable e);
}
