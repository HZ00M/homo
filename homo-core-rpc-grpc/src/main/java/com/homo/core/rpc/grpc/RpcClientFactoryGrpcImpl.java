package com.homo.core.rpc.grpc;

import com.homo.core.common.module.Module;
import com.homo.core.configurable.rpc.RpcClientProperties;
import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClient;
import com.homo.core.facade.rpc.RpcClientFactory;
import io.grpc.ClientInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Slf4j
public class RpcClientFactoryGrpcImpl implements RpcClientFactory,Module {

    @Autowired(required = false)
    private List<ClientInterceptor> clientInterceptorList;
    @Autowired(required = false)
    private RpcClientProperties rpcClientProperties;
    @Override
    public RpcAgentClient newAgent(String hostname, int port, boolean isStateful) {
        RpcClient client = new RpcCallClientGrpcImpl(hostname, port, clientInterceptorList, isStateful,rpcClientProperties);
        RpcAgentClient agentClient = new RpcAgentClientImpl(getServerInfo().serverName, hostname, client, getServerInfo().isStateful, isStateful);
        return agentClient;
    }
}
