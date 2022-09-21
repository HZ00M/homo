package com.homo.core.facade.rpc;

public interface RpcClientFactory {
    /**
     * 创建一个rpc客户端实例
     * @param hostname
     * @param port
     * @return
     */
    RpcAgentClient newAgent(String hostname, int port);
}
