package com.homo.core.gate.tcp.handler;

import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.gate.tcp.GateMessagePackage;
import com.homo.core.gate.tcp.MessageType;
import com.homo.core.gate.tcp.TcpGateDriver;
import com.homo.core.utils.trace.ZipkinUtil;
import io.homo.proto.gate.GateMsg;
import io.netty.channel.ChannelHandlerContext;

public abstract class ProtoLogicHandler extends AbstractLogicHandler<GateMsg>{

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object source) throws Exception {
        GateMessagePackage messagePackage = (GateMessagePackage) source;
        if (messagePackage.getHeader().getType()== MessageType.PROTO.ordinal()){
            GateMsg gateMsg = GateMsg.parseFrom(messagePackage.getBody());
            GateClient gateClient = ctx.channel().attr(TcpGateDriver.clientKey).get();
            ZipkinUtil.startScope(ZipkinUtil.newSRSpan(), span -> process(gateMsg, gateClient), null);
        }else {
            //不是proto数据 交给下一个handler处理
            ctx.fireChannelRead(source);
        }
    }
}
