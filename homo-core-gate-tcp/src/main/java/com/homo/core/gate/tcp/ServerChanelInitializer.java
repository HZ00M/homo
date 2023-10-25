package com.homo.core.gate.tcp;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.configurable.gate.GateTcpProperties;
import com.homo.core.gate.tcp.handler.AbstractGateLogicHandler;
import com.homo.core.gate.tcp.handler.GateDecoderHandler;
import com.homo.core.gate.tcp.handler.GateEncoderHandler;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import reactor.util.function.Tuple3;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
public class ServerChanelInitializer extends ChannelInitializer<SocketChannel> {

    private final GateTcpProperties gateTcpProperties;
    private final GateCommonProperties gateCommonProperties;
    private Tuple3<List<ChannelHandler>, List<AbstractGateLogicHandler>, List<ChannelHandler>> customHandlers;
    public ServerChanelInitializer(Tuple3<List<ChannelHandler>, List<AbstractGateLogicHandler>, List<ChannelHandler>> customHandlers
            , GateTcpProperties gateTcpProperties, GateCommonProperties gateCommonProperties){
        this.customHandlers = customHandlers;
        this.gateTcpProperties = gateTcpProperties;
        this.gateCommonProperties = gateCommonProperties;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(gateTcpProperties.readerIdleTime,gateTcpProperties.writerIdleTime,gateTcpProperties.allIdleTime, TimeUnit.SECONDS));
        pipeline.addLast(new GateEncoderHandler(gateCommonProperties));
        pipeline.addLast(new GateDecoderHandler(gateCommonProperties));
        customHandlers.getT1().forEach(pipeline::addLast);
        customHandlers.getT2().forEach(pipeline::addLast);
        customHandlers.getT3().forEach(pipeline::addLast);
    }
}
