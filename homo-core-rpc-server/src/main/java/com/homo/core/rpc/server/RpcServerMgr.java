package com.homo.core.rpc.server;

import com.homo.core.common.module.Module;
import com.homo.core.facade.rpc.RpcHandler;
import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.facade.rpc.RpcServerFactory;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.ServiceExport;
import com.homo.core.facade.service.ServiceMgr;
import com.homo.core.rpc.base.cache.ServerCache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import java.util.Map;
import java.util.Set;

@Slf4j
public class RpcServerMgr implements Module {
    Set<RpcHandler> rpcHandlers;
    Map<RpcType, RpcServerFactory> rpcServerFactoryMap;
    Map<String, RpcServer> rpcServerMap;

    @Autowired
    @Lazy
    private ServerCache serverCache;//todo 缓存服务器信息

    @Autowired
    @Lazy
    private ServiceMgr serviceMgr;

    public RpcServerMgr(Set<RpcHandler> rpcHandlers, Set<RpcServerFactory> rpcServerFactories) {
        this.rpcHandlers = rpcHandlers;
        for (RpcServerFactory rpcServerFactory : rpcServerFactories) {
            this.rpcServerFactoryMap.put(rpcServerFactory.getType(), rpcServerFactory);
        }
    }

    @Override
    public void init() {
        log.info("RpcServerMgr init rpcHandlers size {}", rpcHandlers.size());
        for (RpcHandler rpcHandler : rpcHandlers) {
            if (rpcHandler instanceof BaseService) {
                BaseService baseService = (BaseService) rpcHandler;
                log.info("RpcServerMgr init service {}", baseService.getServiceName());
                ServiceExport serviceExport = baseService.getServiceExport();
                if (serviceExport != null) {
                    baseService.init(serviceMgr, serverCache, null, null);//todo 实现接口
                    RpcServer rpcServer = RpcServerImpl.doBind(baseService);
                    rpcServerMap.put(baseService.getServiceName(), rpcServer);
                    rpcServerFactoryMap.get(baseService.getServiceExport().DriverType()).startServer(rpcServer);
                    if (serviceExport.isStateful()){
                        getServerInfo().setStateful(true);
                    }
                }
            }
        }
    }
}
