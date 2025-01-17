package com.homo.core.facade.rpc;

import com.homo.core.facade.service.ServiceInfo;

public interface RpcClientFactory {
    /**
     * 创建一个rpc客户端实例
     * @param hostname
     * @param
     * @return
     */
    RpcAgentClient newAgent(String hostname, ServiceInfo serviceInfo);

    RpcType getType();
}
