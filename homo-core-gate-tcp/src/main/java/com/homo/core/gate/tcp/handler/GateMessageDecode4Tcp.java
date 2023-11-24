package com.homo.core.gate.tcp.handler;

import com.homo.core.facade.gate.GateMessageHeader;
import com.homo.core.facade.gate.GateMessagePackage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GateMessageDecode4Tcp extends ByteToMessageDecoder {
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        in.markReaderIndex();
        if (in.readableBytes() < GateMessagePackage.HEAD_LENGTH){
            //不够包头
            in.resetReaderIndex();
            return;
        }
        GateMessageHeader header = GateMessagePackage.getPackHead(in);
        int bodySize = header.getBodySize();
        if (in.readableBytes() < bodySize){
            //半包
            in.resetReaderIndex();
            return;
        }
        in.resetReaderIndex();
        GateMessagePackage messagePackage = GateMessagePackage.getFullPack(in);
        out.add(messagePackage);
    }
}
