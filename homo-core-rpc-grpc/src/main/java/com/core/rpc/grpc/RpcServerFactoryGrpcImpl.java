package com.core.rpc.grpc;

import com.homo.core.rpc.base.trace.SpanInterceptor;
import com.homo.concurrent.thread.ThreadPoolFactory;
import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.facade.rpc.RpcServerFactory;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.utils.trace.ZipkinUtil;
import io.grpc.*;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.EventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioServerSocketChannel;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * rpc的GRPC实现
 */
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
    @Value("${homo.rpc.grpc.message.maxInboundMessageSize:5242880}")//5M
    private int maxInboundMessageSize;
    @Value("${homo.rpc.grpc.message.permitKeepAliveTime:5000}")
    private int permitKeepAliveTime;
    private final Executor executor = ThreadPoolFactory.newThreadPool("RpcServer", corePoolSize, keepLive);
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
        RpcCallServiceGrpcImpl rpcCallService = new RpcCallServiceGrpcImpl(rpcServer);
        try {
            log.info("RpcServerFactoryGrpcImpl start, listening on {}", port);
            final List<ServerInterceptor> interceptors = Optional.ofNullable(serviceServerInterceptors).orElse(Collections.emptyList());
            Server server =
                    NettyServerBuilder.forPort(port)
                            .bossEventLoopGroup(bossGroup)
                            .workerEventLoopGroup(workerGroup)
                            .channelType(NioServerSocketChannel.class)
                            .directExecutor()
                            .maxInboundMessageSize(maxInboundMessageSize)
                            .permitKeepAliveTime(permitKeepAliveTime, TimeUnit.MILLISECONDS)
                            .permitKeepAliveWithoutCalls(true)
                            .intercept(new SpanInterceptor())// 顺序不能反，先添加的拦截器会后执行，需要先添加trace信息才能处理span
                            .intercept(ZipkinUtil.serverInterceptor())
                            .addService(ServerInterceptors.intercept(rpcCallService, interceptors))
                            .addService(ProtoReflectionService.newInstance())
                            .build()
                            .start();
            log.info("ServiceRpcServer started, listening on {} ", port);
            Runtime.getRuntime()
                    .addShutdownHook(new Thread(
                            () ->
                            {
                                //Use stderr here since the logger may have been reset by its JVN shutdown
                                log.error("*** RpcServerFactoryGrpcImpl shutting down gRPC server since JVM is shutting down");
                                RpcServerFactoryGrpcImpl.this.stop(server);
                                log.error("*** RpcServerFactoryGrpcImpl server shut down finish");
                            }
                    ));
        } catch (Exception e) {
            log.error("RpcServerFactoryGrpcImpl startServer error! listening on {} e:", port, e);
            System.exit(-1);
        }
    }

    private void stop(Server server) {
        if (server != null) {
            server.shutdown();
        }
    }

}
