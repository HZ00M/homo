package com.homo.core.facade.rpc;

public interface RpcClientFactory<T> {
    /**
     * 创建一个rpc客户端实例
     * @param hostname
     * @param port
     * @return
     */
    RpcAgentClient<T> newAgent(String hostname, int port ,boolean isStateful);
}
