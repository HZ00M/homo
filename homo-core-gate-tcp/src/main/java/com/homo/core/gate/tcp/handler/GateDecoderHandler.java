package com.homo.core.gate.tcp.handler;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.gate.tcp.GateMessagePackage;
import com.homo.core.gate.tcp.TcpGateDriver;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class GateDecoderHandler extends ByteToMessageDecoder {

    private final GateCommonProperties gateCommonProperties;

    public GateDecoderHandler(GateCommonProperties gateCommonProperties) {
        this.gateCommonProperties = gateCommonProperties;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> nextIn) throws Exception {
        //标记一下当前的readIndex的位置
        in.markReaderIndex();
        //判断一下包头的长度
        if (in.readableBytes() < GateMessage.HEAD_LENGTH) {
            return;
        }
        //解析包头
        int bodySize = in.readInt();
        if (bodySize < 0) {
            //非法数据，关闭连接
            log.error("bodySize < 0 close channel!");
            ctx.close();
        }
        //处理半包
        int readableLength = in.readableBytes();
        int bodySizeRead = 4;
        if (bodySize + GateMessage.HEAD_LENGTH - bodySizeRead > readableLength) {
            //body长度+header长度-已读的int(4B) > 剩余可读长度，说明出现半包，继续等待
            // 重置读取位置(读了bodySize)
            in.resetReaderIndex();
            return;
        }
        //读取到了完整消息，开始解析  & 0xFF 补零扩展，保证补码的一致性，但是表示的十进制发生变化
        int version = in.readByte() & 0xFF;
        //检查客户端版本与服务器版本一致性
        if (version != gateCommonProperties.version) {
            log.error("client version {} != server version {}!", version, gateCommonProperties.version);
            ctx.close();
        }
        int packType = in.readByte() & 0xFF;
        long opTime = in.readLong();
        short sessionId = in.readShort();
        short sendSeq = in.readShort();
        short recvReq = in.readShort();
        byte[] logicBytes = new byte[bodySize];
        /**
         * 将sessionId和packType信息保存起来
         */
        Short oldClientSendSeq = ctx.channel().attr(TcpGateDriver.clientSendReqKey).get();
        if (oldClientSendSeq != null && sendSeq <= oldClientSendSeq) {
            log.error("sendSeq {} <= oldClientSendSeq {}!", sendSeq, oldClientSendSeq);
        }else {
            ctx.channel().attr(TcpGateDriver.clientSendReqKey).set(sendSeq);
        }
        ctx.channel().attr(TcpGateDriver.packType).set(packType);
        in.readBytes(logicBytes);
        GateMessagePackage messagePackage = new GateMessagePackage(logicBytes,version,packType, opTime, sessionId,sendSeq,recvReq);
        nextIn.add(messagePackage);
    }
}
