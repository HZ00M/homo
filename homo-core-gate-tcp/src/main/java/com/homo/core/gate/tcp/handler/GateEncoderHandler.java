package com.homo.core.gate.tcp.handler;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.facade.gate.GateMessageHeader;
import com.homo.core.gate.tcp.TcpGateDriver;
import com.homo.core.utils.exception.HomoError;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class GateEncoderHandler extends MessageToByteEncoder<GateMessage> {

    private final GateCommonProperties gateCommonProperties;

    public GateEncoderHandler(GateCommonProperties gateCommonProperties) {
        this.gateCommonProperties = gateCommonProperties;
    }

    /**
     * 头结构: |bodySize(4B)|version(1B)|packType(1B)|clientSendTime(8B)|sessionId(2B)|sendSeq(2B)|recvReq(2B)
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, GateMessage gateMessage, ByteBuf nextOut) throws Exception {

        byte[] logicBytes = gateMessage.getBody();
        int totalLength = GateMessage.HEAD_LENGTH + logicBytes.length;
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(totalLength);
        try {
            GateMessageHeader header = gateMessage.getHeader();
            Long now = System.currentTimeMillis();
            if (header == null) {
                Integer type = ctx.channel().attr(TcpGateDriver.packType).get();
                Short serverSendSeq = ctx.channel().attr(TcpGateDriver.serverSendSeqKey).get() == null ? 0 : ctx.channel().attr(TcpGateDriver.serverSendSeqKey).get();
                Short clientSendReq = ctx.channel().attr(TcpGateDriver.clientSendReqKey).get() == null ? 0 : ctx.channel().attr(TcpGateDriver.clientSendReqKey).get();
                short newServerSendSeq = (short) (serverSendSeq + 1);
                ctx.channel().attr(TcpGateDriver.serverSendSeqKey).set(newServerSendSeq);

                byteBuf.writeInt(logicBytes.length);//bodySize
                byteBuf.writeByte(gateCommonProperties.version); //version
                byteBuf.writeByte(type);//读取保存在channel中的packType
                byteBuf.writeLong(now);
                byteBuf.writeShort(Short.MIN_VALUE);//读取保存在channel中的sessionId  //todo sessionId
                byteBuf.writeShort(newServerSendSeq);
                byteBuf.writeShort(clientSendReq); //对于服务器来说是客户端的sendReq
            } else {
                byteBuf.writeInt(header.getBodySize());
                byteBuf.writeByte(header.getVersion());
                byteBuf.writeByte(header.getType());
                byteBuf.writeLong(header.getOpTime());
                byteBuf.writeShort(header.getSessionId());
                byteBuf.writeShort(header.getSendSeq());
                byteBuf.writeShort(header.getRecvSeq());
            }
            byteBuf.writeBytes(logicBytes);
            nextOut.writeBytes(byteBuf);
        } catch (Exception e) {
            log.error("TcpEncoderHandler encode error ", e);
            throw HomoError.throwError(HomoError.gateError, "TcpEncoderHandler encode error");
        } finally {
            //自己分配的ByteBuf要自己释放
            ReferenceCountUtil.release(byteBuf);
        }
    }
}
