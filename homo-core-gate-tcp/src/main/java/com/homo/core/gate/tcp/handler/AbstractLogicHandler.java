package com.homo.core.gate.tcp.handler;

import com.homo.core.facade.gate.GateClient;
import com.homo.core.gate.tcp.TcpGateDriver;
import com.homo.core.utils.trace.ZipkinUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;



@ChannelHandler.Sharable
@Log4j2
public abstract class AbstractLogicHandler<T> extends ChannelInboundHandlerAdapter {
    public abstract void process(T data, GateClient gateClient) throws Exception;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object source) throws Exception {
        T data = (T) source;
        GateClient gateClient = ctx.channel().attr(TcpGateDriver.clientKey).get();
        ZipkinUtil.startScope(ZipkinUtil.newSRSpan(), span -> process(data, gateClient), null);
    }

}
