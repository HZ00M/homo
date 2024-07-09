package com.homo.core.gate.tcp;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.configurable.gate.GateTcpProperties;
import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateDriver;
import com.homo.core.facade.gate.GateServer;
import com.homo.core.gate.tcp.handler.AbstractGateLogicHandler;
import com.homo.core.utils.concurrent.thread.ThreadPoolFactory;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.module.DriverModule;
import com.homo.core.utils.rector.Homo;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TcpGateDriver implements GateDriver, DriverModule {

    @Autowired(required = false)
    private GateTcpProperties gateTcpProperties;
    @Autowired(required = false)
    private GateCommonProperties gateCommonProperties;
    private GateServer gateServer;
    /**
     * 监听的serverChannel
     */
    private Channel serverChannel;
    /**
     * 保持每个客户端连接
     */
    private Map<GateClient, Channel> clientMap = new HashMap<>();

    public static AttributeKey<GateClient> clientKey = AttributeKey.valueOf("client");
    public static AttributeKey<Short> sessionIdKey = AttributeKey.valueOf("sessionId");
    public static AttributeKey<Integer> packType = AttributeKey.valueOf("packType");
    public static AttributeKey<Short> serverSendSeqKey = AttributeKey.valueOf("serverSendSeq");
    public static AttributeKey<Short> recvConfirmSeqKey = AttributeKey.valueOf("recvConfirmSeq");
    public static AttributeKey<Short> clientSendReqKey = AttributeKey.valueOf("clientSendReq");

    /**
     * 服务器运行状态
     */
    private volatile boolean isRunning = false;

    /**
     * 处理Accept连接事件的线程，设置成1即可，netty处理连接时间默认为单线程
     */
    private Executor bossExecutor = ThreadPoolFactory.newThreadPool("gatePool", 1, 0);

    private EventLoopGroup bossGroup;

    private EventLoopGroup workGroup;

    private Tuple3<List<ChannelHandler>, List<AbstractGateLogicHandler>, List<ChannelHandler>> customHandlers = Tuples.of(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());


    public void moduleInit() {
        if (Epoll.isAvailable()) {
            bossGroup = new EpollEventLoopGroup(gateTcpProperties.bossNum, bossFactory);
            workGroup = new EpollEventLoopGroup(gateTcpProperties.workNum,workFactory);
            log.info("TcpGateDriver initialize using epoll");
        } else {
            bossGroup = new NioEventLoopGroup(gateTcpProperties.bossNum, bossFactory);
            workGroup = new NioEventLoopGroup(gateTcpProperties.workNum,workFactory);
            log.info("TcpGateDriver initialize not use epoll");
        }
    }

    @Override
    public void startGate(GateServer gateServer) {
        try {
            this.gateServer = gateServer;
            ChannelFuture channelFuture = new ServerBootstrap()//创建ServerBootstrap实例
                    .group(bossGroup, workGroup)//初始化ServerBootstrap线程组
                    .channel(Epoll.isAvailable() ? EpollServerSocketChannel.class : NioServerSocketChannel.class)//设置将要被实例化的ServerChannel类
                    .childHandler(new ServerChanelInitializer(customHandlers, gateTcpProperties, gateCommonProperties))//在ServerChannelInitializer中初始化ChannelPipeline责任链，并添加到serverBootstrap中
                    .option(ChannelOption.SO_BACKLOG, 1024)//标识当服务处理线程全满时，用于临时存放已三次握手的请求的队列的最大长度
                    .childOption(ChannelOption.SO_KEEPALIVE, true)//是否启用心跳保活机制
                    .bind(gateServer.getPort()).sync();//绑定端口后，开启监听
            if (channelFuture.isSuccess()) {
                log.info("TcpGateDriver {} startGateServer success listener port {}", gateServer.getName(), gateServer.getPort());
                serverChannel = channelFuture.channel();
                isRunning = true;
            }

        } catch (Exception e) {
            log.error("TcpGateDriver name {} port {} startGateServer fail", gateServer.getName(), gateServer.getPort(), e);
            System.exit(-1);
        }
    }


    @Override
    public void closeGate() throws Exception {
        if (!isRunning) {
            throw HomoError.throwError(HomoError.gateError, "gate server not running");
        }
        this.isRunning = false;
        for (Map.Entry<GateClient, Channel> clientChannelEntry : clientMap.entrySet()) {
            GateClient gateClient = clientChannelEntry.getKey();
            Channel channel = clientChannelEntry.getValue();
            gateClient.onClose("GateServer close");
            channel.close();
        }
        clientMap.clear();
        if (serverChannel != null) {
            serverChannel.close();
            serverChannel = null;
        }
        Future<?> workGroupFuture = workGroup.shutdownGracefully().await();
        if (!workGroupFuture.isSuccess()) {
            throw HomoError.throwError(HomoError.gateError, "workGroup stop fail");
        }
        Future<?> bossGroupFuture = bossGroup.shutdownGracefully().await();
        if (!bossGroupFuture.isSuccess()) {
            throw HomoError.throwError(HomoError.gateError, "bossGroup stop fail");
        }
    }

    @Override
    public <T> Homo<Boolean> sendToClientComplete(GateClient gateClient, T msg) {
        return Homo.warp(homoSink -> {
            Channel channel = clientMap.get(gateClient);
            if (channel != null) {
                ChannelFuture future = channel.writeAndFlush(msg);
                future.addListener(future1 -> {
                    homoSink.success(future1.isSuccess());
                });
            } else {
                log.info("sendToClient channel is null gateClient {}",gateClient.name());
                homoSink.success(false);
            }
        });
    }
    @Override
    public <T> Homo<Boolean> sendToClient(GateClient gateClient, T msg) {
        return Homo.warp(homoSink -> {
            Channel channel = clientMap.get(gateClient);
            if (channel != null && channel.isActive()) {
                ChannelFuture future = channel.writeAndFlush(msg);
                homoSink.success(true);
            } else {
                log.info("sendToClient channel is null gateClient {}",gateClient.name());
                homoSink.success(false);
            }
        });
    }

    @Override
    public <T> Homo<Boolean> broadcast(T msg) {
        for (Channel channel : clientMap.values()) {
            channel.writeAndFlush(msg);
        }
        return Homo.result(true);
    }

    @Override
    public void closeGateClient(GateClient gateClient) {
        Channel channel = clientMap.get(gateClient);
        if (channel != null) {
            channel.close();
        } else {
            log.error("closeGateClient channel not found, gateClient {}", gateClient);
        }
        gateClient.onClose("do closeGateClient");
    }

    public void createConnection(ChannelHandlerContext context, String addr, int port) {
        log.info("createConnection start addr {} port {}", addr, port);
        Channel channel = context.channel();
        GateClient gateClient = gateServer.newClient(addr, port);
        clientMap.put(gateClient, channel);
        channel.attr(clientKey).set(gateClient);
        gateClient.onOpen();
        log.info("createConnection end addr {} port {} ", addr, port);
    }

    public GateClient getGateClient(Channel channel) {
        return channel.attr(clientKey).get();
    }

    public void closeConnection(ChannelHandlerContext context, String reason) {
        Channel channel = context.channel();
        GateClient gateClient = getGateClient(channel);
        if (gateClient != null) {
            gateClient.onClose(reason);
            clientMap.remove(gateClient);
        } else {
            log.error("tpcGateDriver closeConnection can not find gateClient");
        }
    }

    public void registerBeforeHandler(ChannelHandler handler) {
        customHandlers.getT1().add(handler);
    }

    public <T> void registerPostHandler(AbstractGateLogicHandler<T> handler) {
        customHandlers.getT2().add(handler);
    }

    public void registerAfterHandler(ChannelHandler handler) {
        customHandlers.getT3().add(handler);
    }

    ThreadFactory bossFactory = new ThreadFactory() {
        private AtomicInteger atomicInteger = new AtomicInteger();
        int index ;
        @Override
        public Thread newThread(@NotNull Runnable r) {
            index = atomicInteger.incrementAndGet();
            Thread t = new Thread(r, "TcpBoss" +index);
            return t;
        }
    };
    ThreadFactory workFactory = new ThreadFactory() {
        private AtomicInteger atomicInteger = new AtomicInteger();
        int index ;
        @Override
        public Thread newThread(@NotNull Runnable r) {
            index = atomicInteger.incrementAndGet();
            Thread t = new Thread(r, "TcpWorker" +index);
            return t;
        }
    };
}