package com.homo.core.rpc.grpc;

import com.homo.core.configurable.rpc.RpcGrpcClientProperties;
import com.homo.core.utils.module.Module;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClient;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.utils.module.RootModule;
import io.grpc.ClientInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class RpcClientFactoryGrpcImpl implements RpcClientFactory, Module {
    @Autowired
    private RootModule rootModule;
    @Autowired(required = false)
    private List<ClientInterceptor> clientInterceptorList;
    @Autowired(required = false)
    private RpcGrpcClientProperties rpcGrpcClientProperties;

    @Override
    public RpcAgentClient newAgent(String hostname, int port, boolean isStateful) {
        RpcClient client = new RpcCallClientGrpcImpl(hostname, port, clientInterceptorList, isStateful, rpcGrpcClientProperties);
        RpcAgentClient agentClient = new RpcAgentClientImpl(rootModule.getServerInfo().serverName, hostname, client, rootModule.getServerInfo().isStateful, isStateful);
        return agentClient;
    }

    @Override
    public RpcType getType() {
        return RpcType.grpc;
    }
}
