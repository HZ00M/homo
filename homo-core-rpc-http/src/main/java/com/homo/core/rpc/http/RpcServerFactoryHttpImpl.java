package com.homo.core.rpc.http;

import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.facade.rpc.RpcServerFactory;
import com.homo.core.facade.rpc.RpcType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * http rpc服务工厂类
 */
@Slf4j
public class RpcServerFactoryHttpImpl implements RpcServerFactory{
    @Autowired(required = false)
    private ApplicationContext applicationContext;

    public static Map<Integer,HttpServer> httpServerMap = new ConcurrentHashMap<>();

    @Override
    public RpcType getType() {
        return RpcType.http;
    }

    @Override
    public void startServer(RpcServer rpcServer) {
        HttpServer httpServer = new HttpServer(rpcServer);
        httpServer.start(applicationContext);
        httpServerMap.put(rpcServer.getPort(),httpServer);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            stopServer(httpServer);
        }));
    }

    public void stopServer(HttpServer httpServer) {
        if (httpServer!=null){
            System.err.println("*** shutting down httpServer since JVM is shutting down");
            httpServerMap.remove(httpServer.getPort());
            httpServer.stop();
            System.err.println("*** httpServer shut down");
        }
    }
}
