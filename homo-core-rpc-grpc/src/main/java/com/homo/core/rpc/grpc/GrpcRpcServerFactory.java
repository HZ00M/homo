package com.homo.core.rpc.grpc;

import com.homo.core.configurable.rpc.RpcGrpcServerProperties;
import com.homo.core.facade.rpc.RpcServer;
import com.homo.core.facade.rpc.RpcServerFactory;
import com.homo.core.facade.rpc.RpcType;
import com.homo.core.rpc.base.trace.SpanInterceptor;
import com.homo.core.utils.concurrent.thread.ThreadPoolFactory;
import com.homo.core.utils.trace.ZipkinUtil;
import io.grpc.Server;
import io.grpc.ServerInterceptor;
import io.grpc.ServerInterceptors;
import io.grpc.netty.shaded.io.grpc.netty.NettyServerBuilder;
import io.grpc.netty.shaded.io.netty.channel.EventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.nio.NioEventLoopGroup;
import io.grpc.netty.shaded.io.netty.channel.socket.nio.NioServerSocketChannel;
import io.grpc.protobuf.services.ProtoReflectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 * rpc的GRPC实现
 */
@Slf4j
public class GrpcRpcServerFactory implements RpcServerFactory {

    @Autowired(required = false)
    private RpcGrpcServerProperties rpcGrpcServerProperties;

    private Executor executor ;
    private EventLoopGroup bossGroup ;
    private EventLoopGroup workerGroup ;

    @Override
    public void moduleInit(){
        executor =  ThreadPoolFactory.newThreadPool("RpcServer", rpcGrpcServerProperties.getCorePoolSize(), rpcGrpcServerProperties.getKeepLive());
        bossGroup = new NioEventLoopGroup(rpcGrpcServerProperties.getBoosThreadSize(), executor);
        workerGroup = new NioEventLoopGroup(rpcGrpcServerProperties.getWorkerThreadSize(), executor);
    }

    @Autowired(required = false)
    private List<ServerInterceptor> serviceServerInterceptors;

    @Override
    public RpcType getType() {
        return RpcType.grpc;
    }

    @Override
    public void startServer(RpcServer rpcServer) {
        int port = rpcServer.getPort();
        GrpcRpcCallService rpcCallService = new GrpcRpcCallService(rpcServer);
        try {
            log.info("RpcServerFactoryGrpcImpl start, listening on {}", port);
            final List<ServerInterceptor> interceptors = Optional.ofNullable(serviceServerInterceptors).orElse(Collections.emptyList());
            Server server =
                    NettyServerBuilder.forPort(port)
                            .bossEventLoopGroup(bossGroup)
                            .workerEventLoopGroup(workerGroup)
                            .channelType(NioServerSocketChannel.class)
                            .directExecutor()
                            .maxInboundMessageSize(rpcGrpcServerProperties.getMaxInboundMessageSize())
                            .permitKeepAliveTime(rpcGrpcServerProperties.getPermitKeepAliveTime(), TimeUnit.MILLISECONDS)
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
                                GrpcRpcServerFactory.this.stop(server);
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
