package com.homo.core.rpc.server;

import com.homo.core.facade.rpc.RpcContent;
import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.facade.service.Service;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

/**
 * rpc服务器实现
 */
@Slf4j
public class RpcServerImpl implements RpcServer {

    private final Service actualService;

    public static RpcServer doBind(Service actualService) {
        RpcServerImpl delegate = new RpcServerImpl(actualService);
        return delegate;
    }

    private RpcServerImpl(Service actualService) {
        log.info(
                "RpcServerImpl serviceName {} port {} class name {}",
                actualService.getTagName(),
                actualService.getPort(),
                actualService.getClass().getName());
        this.actualService = actualService;
    }

    @Override
    public String getHostName() {
        return actualService.getHostName();
    }

    @Override
    public int getPort() {
        return actualService.getPort();
    }

    @Override
    public RpcType getType() {
        return actualService.getType();
    }

    @Override
    public Homo onCall(String srcService, String funName, RpcContent param)  {
        if (actualService == null) {
            log.warn("RpcServerImpl onCall service is null, funName {}", funName);
            return Homo.result(null);
        }
        return actualService.callFun(srcService, funName, param);
    }


}
