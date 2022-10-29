package com.homo.core.gate.tcp;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

import java.util.List;

@ChannelHandler.Sharable
public class ServerChanelInitializer extends ChannelInitializer<SocketChannel> {

    List<ChannelHandler> handlers;
    public ServerChanelInitializer(List<ChannelHandler> handlers){
        this.handlers = handlers;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        for (ChannelHandler handler : handlers) {
            pipeline.addLast(handler.getClass().getSimpleName(),handler);
        }
    }
}
