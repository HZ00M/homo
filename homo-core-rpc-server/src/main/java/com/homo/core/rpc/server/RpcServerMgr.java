package com.homo.core.rpc.server;

import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.facade.rpc.RpcServerFactory;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.rpc.base.service.ServiceMgr;
import com.homo.core.utils.module.Module;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Slf4j
public class RpcServerMgr implements Module {
    Map<RpcType, RpcServerFactory> rpcServerFactoryMap = new HashMap<>();
    Map<String, RpcServer> rpcServerMap = new HashMap<>();
    @Autowired(required = false)
    private ServiceMgr serviceMgr;
    @Autowired(required = false)
    private Set<RpcServerFactory> rpcServerFactories;

    @Override
    public void afterAllModuleInit() {
        for (RpcServerFactory rpcServerFactory : rpcServerFactories) {
            log.info("RpcServer support rpcServerFactory type {} ",rpcServerFactory.getType());
            this.rpcServerFactoryMap.put(rpcServerFactory.getType(), rpcServerFactory);
        }
        serviceMgr.getServices().forEach(service -> {
            RpcServer rpcServer = RpcServerImpl.doBind(service);
            rpcServerMap.put(service.getTagName(),rpcServer);
            rpcServerFactoryMap.get(service.getType()).startServer(
                    rpcServer);
        });
    }
}
