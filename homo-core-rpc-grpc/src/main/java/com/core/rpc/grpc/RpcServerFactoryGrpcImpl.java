package com.core.rpc.grpc;

import com.homo.concurrent.thread.ThreadPoolFactory;
import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.facade.rpc.RpcServerFactory;
import com.homo.core.facade.rpc.RpcType;
import io.grpc.ServerInterceptor;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.concurrent.Executor;

@Slf4j
public class RpcServerFactoryGrpcImpl implements RpcServerFactory {
    @Value("${homo.rpc.grpc.thread.size:3}")
    private int corePoolSize;
    @Value("${homo.rpc.grpc.thread.keepLive:0}")
    private int keepLive;
    @Value("${homo.rpc.grpc.boss.thread.size:1}")
    private int boosThreadSize;
    @Value("${homo.rpc.grpc.worker.thread.size:2}")
    private int workerThreadSize;
    private Executor executor = ThreadPoolFactory.newThreadPool("RpcServer", corePoolSize, keepLive);
    EventLoopGroup bossGroup = new NioEventLoopGroup(boosThreadSize, executor);
    EventLoopGroup workerGroup = new NioEventLoopGroup(workerThreadSize, executor);

    @Autowired(required = false)
    private List<ServerInterceptor> serviceServerInterceptors;

    @Override
    public RpcType getType() {
        return RpcType.grpc;
    }

    @Override
    public void startServer(RpcServer rpcServer) {
        int port = rpcServer.getPort();
    }
}
