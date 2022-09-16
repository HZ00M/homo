package com.homo.core.rpc.server;

import brave.Span;
import com.homo.core.facade.serial.RpcContent;
import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.facade.service.Service;
import com.homo.core.utils.rector.Homo;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;
import reactor.util.function.Tuples;

/**
 * rpc服务器实现
 */
@Slf4j
public class RpcServerImpl implements RpcServer {

    private final Service actualService;

    private CallDispatcher callDispatcher;

    public static RpcServer doBind(Service actualService){
        RpcServerImpl delegate = new RpcServerImpl(actualService);
        return delegate;
    }

    private RpcServerImpl(Service actualService) {
        log.info(
                "RpcServerImpl serviceName {} port {} class name {}",
                actualService.getServiceName(),
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
    public String getType() {
        return actualService.getType();
    }

    @Override
    public Homo onCall(String srcService, String funName, RpcContent param)  {
        Span span = ZipkinUtil.getTracing().tracer().currentSpan();
        if (span != null) {
            span.name(funName);
        }
        if (actualService == null) {
            log.warn("RpcServerImpl onCall service is null, funName_{}", funName);
            return Homo.result( Tuples.of(DriverRpcBack.NO_FUNCTION, null));

        }
        return actualService.callFun(srcService, funName, param);
    }

//    @Override
//    public Homo processError(String msgId, Throwable e) {
//        return actualService.processError(msgId, e);
//    }

}
