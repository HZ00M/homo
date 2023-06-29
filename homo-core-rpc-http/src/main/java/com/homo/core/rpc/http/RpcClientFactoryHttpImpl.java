package com.homo.core.rpc.http;

import com.homo.core.common.module.Module;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.facade.rpc.RpcType;

//todo 实现http rpc客户端
public class RpcClientFactoryHttpImpl implements RpcClientFactory, Module {
    @Override
    public RpcAgentClient newAgent(String hostname, int port, boolean isStateful) {
        return null;
    }

    @Override
    public RpcType getType() {
        return RpcType.http;
    }
}
