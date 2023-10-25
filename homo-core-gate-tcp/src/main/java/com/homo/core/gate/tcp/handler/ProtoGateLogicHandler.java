package com.homo.core.gate.tcp.handler;

import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateMessageHeader;
import com.homo.core.gate.tcp.GateMessagePackage;
import com.homo.core.gate.tcp.GateMessageType;
import com.homo.core.gate.tcp.TcpGateDriver;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.client.Msg;
import io.netty.channel.ChannelHandlerContext;

public abstract class ProtoGateLogicHandler extends AbstractGateLogicHandler<Msg> {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object source) throws Exception {
        GateMessagePackage messagePackage = (GateMessagePackage) source;
        GateMessageHeader header = messagePackage.getHeader();
        if (header.getType() == GateMessageType.PROTO.ordinal()) {
            Msg msg = Msg.parseFrom(messagePackage.getBody());
            GateClient gateClient = ctx.channel().attr(TcpGateDriver.clientKey).get();
            ZipkinUtil.startScope(ZipkinUtil.newSRSpan(), span -> doProcess(msg, gateClient,header), null);
        } else {
            //不是proto数据 交给下一个handler处理
            ctx.fireChannelRead(source);
        }
    }
}
