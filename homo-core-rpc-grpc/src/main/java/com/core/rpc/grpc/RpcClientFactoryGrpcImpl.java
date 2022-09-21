package com.core.rpc.grpc;

import com.homo.core.common.module.Module;
import com.homo.core.configurable.rpc.RpcClientProperties;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClient;
import com.homo.core.facade.rpc.RpcClientFactory;
import io.grpc.ClientInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RpcClientFactoryGrpcImpl implements RpcClientFactory,Module {

    @Autowired
    private List<ClientInterceptor> clientInterceptorList;
    @Autowired
    private RpcClientProperties rpcClientProperties;
    @Override
    public RpcAgentClient newAgent(String hostname, int port, boolean isStateful) {
        RpcClient client = new RpcCallClientGrpcImpl(hostname, port, clientInterceptorList, isStateful,rpcClientProperties);
        RpcAgentClient agentClient = new RpcAgentClientImpl(getServerInfo().serverName, hostname, client, getServerInfo().isStateful, isStateful);
        return agentClient;
    }
}
