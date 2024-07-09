package com.homo.core.rpc.client;

import com.homo.core.facade.rpc.RpcAgentClient;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.rpc.base.utils.ServiceUtil;
import com.homo.core.utils.exception.HomoException;
import com.homo.core.utils.module.ServiceModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class RpcClientMgr implements ServiceModule, ApplicationContextAware {
    private Map<RpcType, RpcClientFactory> rpcClientFactoryMap = new HashMap<>();
    @Autowired(required = false)
    private ServiceMgr serviceMgr;
    private final Map<String, RpcAgentClient> rpcAgentClientMap = new HashMap();
    private ApplicationContext applicationContext;

    @Override
    public void moduleInit() {
        Map<String, RpcClientFactory> rpcClientImplMap = applicationContext.getBeansOfType(RpcClientFactory.class);
        for (RpcClientFactory clientFactory : rpcClientImplMap.values()) {
            rpcClientFactoryMap.put(clientFactory.getType(), clientFactory);
        }
    }

    public RpcAgentClient getGrpcAgentClient(String realHostName) throws HomoException {
        boolean statefulService = ServiceUtil.isStatefulService(realHostName);
        return getGrpcAgentClient(realHostName, statefulService);
    }

    public RpcAgentClient getGrpcAgentClient(String realHostName, boolean isStateful) throws HomoException {
        RpcAgentClient rpcAgentClient;
        synchronized (RpcAgentClient.class) {
            //k8s域名格式: hostname(tagName带-n序号).tagName.namespaceId.svc.cluster.local
            rpcAgentClient = rpcAgentClientMap.computeIfAbsent(realHostName, s -> {
                String host = ServiceUtil.getServiceHostNameByRealHost(realHostName);
                int port = ServiceUtil.getServicePortByRealHost(realHostName);
                log.info("new agent begin  hostname {} port {} isStateful {}", realHostName, port, isStateful);

                RpcAgentClient newAgent = rpcClientFactoryMap.get(RpcType.grpc).newAgent(host, port, isStateful);
                log.info("new agent finish  hostname {} port {} isStateful {}", realHostName, port, isStateful);
                return newAgent;
            });
        }
        return rpcAgentClient;
    }



    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
