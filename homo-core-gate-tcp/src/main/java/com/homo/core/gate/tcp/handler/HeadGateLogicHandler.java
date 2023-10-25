package com.homo.core.gate.tcp.handler;

import com.homo.core.facade.gate.GateMessageHeader;
import com.homo.core.gate.tcp.GateMessagePackage;
import com.homo.core.gate.tcp.TcpGateDriver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public  class HeadGateLogicHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object source) throws Exception {
        GateMessagePackage messagePackage = (GateMessagePackage) source;
        GateMessageHeader header = messagePackage.getHeader();
        long opTime = header.getOpTime();
        short recvSeq = header.getRecvSeq();
        ctx.channel().attr(TcpGateDriver.recvConfirmSeqKey).set(recvSeq);
        ctx.fireChannelRead(source);
    }
     
}
