package com.homo.core.rpc.grpc;

import com.homo.core.utils.module.Module;
import com.homo.core.configurable.rpc.RpcGrpcClientProperties;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClient;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.facade.rpc.RpcType;
import io.grpc.ClientInterceptor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Log4j2
public class RpcClientFactoryGrpcImpl implements RpcClientFactory, Module {

    @Autowired(required = false)
    private List<ClientInterceptor> clientInterceptorList;
    @Autowired(required = false)
    private RpcGrpcClientProperties rpcGrpcClientProperties;

    @Override
    public RpcAgentClient newAgent(String hostname, int port, boolean isStateful) {
        RpcClient client = new RpcCallClientGrpcImpl(hostname, port, clientInterceptorList, isStateful, rpcGrpcClientProperties);
        RpcAgentClient agentClient = new RpcAgentClientImpl(getServerInfo().serverName, hostname, client, getServerInfo().isStateful, isStateful);
        return agentClient;
    }

    @Override
    public RpcType getType() {
        return RpcType.grpc;
    }
}
