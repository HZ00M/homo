package com.homo.core.gate.tcp.handler;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.facade.excption.HomoError;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.gate.tcp.GateMessagePackage;
import com.homo.core.gate.tcp.TcpGateDriver;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class EncoderHandler extends MessageToByteEncoder<GateMessage> {

    private final GateCommonProperties gateCommonProperties;

    public EncoderHandler(GateCommonProperties gateCommonProperties) {
        this.gateCommonProperties = gateCommonProperties;
    }

    /**
     * 头结构: |bodySize(4B)|version(1B)|packType(1B)|clientSendTime(8B)|sessionId(2B)|sendSeq(2B)|recvSeq(2B)|serverSendTime(8B) (后面俩个字段暂时填充)
     */
    @Override
    protected void encode(ChannelHandlerContext ctx, GateMessage gateMessage, ByteBuf nextOut) throws Exception {

        byte[] logicBytes = gateMessage.getBody();
        int totalLength = GateMessage.HEAD_LENGTH + logicBytes.length;
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(totalLength);
        try {
            GateMessage.Header header = gateMessage.getHeader();
            if (header == null) {
                byteBuf.writeInt(logicBytes.length);
                byteBuf.writeByte(gateCommonProperties.version);
                byteBuf.writeByte(ctx.channel().attr(TcpGateDriver.packType).get());//读取保存在channel中的packType
                byteBuf.writeLong(System.currentTimeMillis());
                byteBuf.writeShort(ctx.channel().attr(TcpGateDriver.sessionIdKey).get());//读取保存在channel中的sessionId
                Short oldSeq = ctx.channel().attr(TcpGateDriver.serverSeqKey).get();
                short incr = 1;
                short newSeq = (short) (oldSeq + incr);
                ctx.channel().attr(TcpGateDriver.serverSeqKey).set(newSeq);
                byteBuf.writeShort(newSeq);
            } else {
                byteBuf.writeInt(header.getBodySize());
                byteBuf.writeByte(header.getVersion());
                byteBuf.writeByte(header.getType());
                byteBuf.writeLong(header.getOpTime());
                byteBuf.writeShort(header.getSessionId());
                byteBuf.writeShort(header.getOpSeq());
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
