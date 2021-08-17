package com.homo.core.facade.gate;

/**
 * 客户端连接代理
 */
public interface GateServer {
    GateClient newClient(String addr,int port);

    int getPort();

    String getAddr();
}
