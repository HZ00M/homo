package com.homo.core.gate.tcp.handler;

import com.homo.core.facade.gate.GateMessagePackage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class GateMessageEncode4Tcp extends MessageToByteEncoder<GateMessagePackage> {
    @Override
    protected void encode(ChannelHandlerContext ctx, GateMessagePackage msg, ByteBuf out) throws Exception {
        ByteBuf buf = msg.makeBuf();
        out.writeBytes(buf);
        buf.release();
    }
}
