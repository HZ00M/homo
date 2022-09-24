package com.homo.core.rpc.server;

import com.homo.core.common.module.Module;
import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.facade.rpc.RpcServerFactory;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.rpc.base.service.ServiceMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Map;
import java.util.Set;

@Slf4j
public class RpcServerMgr implements Module {
    Set<Class<?>> rpcHandlers;//todo 可能没用了
    Map<RpcType, RpcServerFactory> rpcServerFactoryMap;
    Map<String, RpcServer> rpcServerMap;
    @Autowired
    @Lazy
    private ServiceMgr serviceMgr;

    public RpcServerMgr(Set<Class<?>> rpcHandlerClazz, Set<RpcServerFactory> rpcServerFactories) {
        this.rpcHandlers = rpcHandlerClazz;
        for (RpcServerFactory rpcServerFactory : rpcServerFactories) {
            this.rpcServerFactoryMap.put(rpcServerFactory.getType(), rpcServerFactory);
        }
    }

    @Override
    public void init() {
        log.info("RpcServerMgr init rpcHandlers size {}", rpcHandlers.size());
        serviceMgr.getServices().forEach(service -> {
            RpcServer rpcServer = RpcServerImpl.doBind(service);
            rpcServerMap.put(service.getServiceName(),rpcServer);
            rpcServerFactoryMap.get(service.getType()).startServer(
                    rpcServer);
        });
    }
}
