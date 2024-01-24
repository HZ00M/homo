package com.homo.core.gate.tcp;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.configurable.gate.GateTcpProperties;
import com.homo.core.gate.tcp.handler.AbstractGateLogicHandler;
import com.homo.core.gate.tcp.handler.GateDecoderHandler;
import com.homo.core.gate.tcp.handler.GateEncoderHandler;
import com.homo.core.gate.tcp.handler.HeartbeatHandler;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.jetbrains.annotations.NotNull;
import reactor.util.function.Tuple3;

import java.util.List;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class ServerChanelInitializer extends ChannelInitializer<SocketChannel> {

    private final GateTcpProperties gateTcpProperties;
    private final GateCommonProperties gateCommonProperties;
    private Tuple3<List<ChannelHandler>, List<AbstractGateLogicHandler>, List<ChannelHandler>> customHandlers;

    public ServerChanelInitializer(Tuple3<List<ChannelHandler>, List<AbstractGateLogicHandler>, List<ChannelHandler>> customHandlers
            , GateTcpProperties gateTcpProperties, GateCommonProperties gateCommonProperties) {
        this.customHandlers = customHandlers;
        this.gateTcpProperties = gateTcpProperties;
        this.gateCommonProperties = gateCommonProperties;
    }

    EventLoopGroup heartBeatGroup = new DefaultEventLoopGroup(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NotNull Runnable runnable) {
            return new Thread(runnable, "heartBeatGroup");
        }
    });

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(gateTcpProperties.readerIdleTime, gateTcpProperties.writerIdleTime, gateTcpProperties.allIdleTime, TimeUnit.SECONDS));
        pipeline.addLast(new GateEncoderHandler(gateCommonProperties));
        pipeline.addLast(new GateDecoderHandler(gateCommonProperties));
        /**
         * 当 handler 中绑定的 Group 不同时，需要切换 Group 来执行不同的任务
         * static void invokeChannelRead(final AbstractChannelHandlerContext next, Object msg) {
         *     final Object m = next.pipeline.touch(ObjectUtil.checkNotNull(msg, "msg"), next);
         *     // 获得下一个EventLoop, excutor 即为 EventLoopGroup
         *     EventExecutor executor = next.executor();
         *
         *     // 如果下一个EventLoop 在当前的 EventLoopGroup中
         *     if (executor.inEventLoop()) {
         *         // 使用当前 EventLoopGroup 中的 EventLoop 来处理任务
         *         next.invokeChannelRead(m);
         *     } else {
         *         // 否则让另一个 EventLoopGroup 中的 EventLoop 来创建任务并执行
         *         executor.execute(new Runnable() {
         *             public void run() {
         *                 next.invokeChannelRead(m);
         *             }
         *         });
         *     }
         * }
         * 如果两个 handler 绑定的是同一个 EventLoopGroup，那么就直接调用
         * 否则，把要调用的代码封装为一个任务对象，由下一个 handler 的 EventLoopGroup 来调用
         */
        pipeline.addLast(heartBeatGroup, new HeartbeatHandler());
        customHandlers.getT1().forEach(pipeline::addLast);
        customHandlers.getT2().forEach(pipeline::addLast);
        customHandlers.getT3().forEach(pipeline::addLast);
    }
}
