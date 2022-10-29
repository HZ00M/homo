package com.homo.core.gate.tcp.handler;

import com.homo.core.utils.trace.ZipkinUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import lombok.extern.log4j.Log4j2;


@Log4j2
public abstract class TcpHandler<T> extends ChannelInboundHandlerAdapter {

    public abstract void process(T data);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object source) throws Exception {
        T data = (T) source;
        ZipkinUtil.startScope(ZipkinUtil.newSRSpan(), span -> process(data), null);
    }

}
