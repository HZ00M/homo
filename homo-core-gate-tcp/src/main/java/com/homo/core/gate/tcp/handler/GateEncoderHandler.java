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
        int bodyLength = logicBytes == null ? 0 : logicBytes.length;
        int totalLength = GateMessage.HEAD_LENGTH + bodyLength;
        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(totalLength);//默认使用直接内存池
        try {
            GateMessageHeader header = gateMessage.getHeader();
            Long now = System.currentTimeMillis();
            if (header == null) {
                Integer packType = ctx.channel().attr(TcpGateDriver.packType).get();
                Short serverSendSeq = ctx.channel().attr(TcpGateDriver.serverSendSeqKey).get() == null ? 0 : ctx.channel().attr(TcpGateDriver.serverSendSeqKey).get();
                Short clientSendReq = ctx.channel().attr(TcpGateDriver.clientSendReqKey).get() == null ? 0 : ctx.channel().attr(TcpGateDriver.clientSendReqKey).get();
                Short sessionId = ctx.channel().attr(TcpGateDriver.sessionIdKey).get() == null ? 0 : ctx.channel().attr(TcpGateDriver.sessionIdKey).get();
                short newServerSendSeq = (short) (serverSendSeq + 1);
                ctx.channel().attr(TcpGateDriver.serverSendSeqKey).set(newServerSendSeq);

                byteBuf.writeInt(bodyLength);//bodySize消息长度
                byteBuf.writeByte(gateCommonProperties.version); //version服务器与客户端版本对齐
                byteBuf.writeByte(packType);//packType 包类型 读取保存在channel中的 proto或json
                byteBuf.writeLong(now);//发送的时间戳
                byteBuf.writeShort(sessionId);//sessionId 客户端请求与响应一一对应
                byteBuf.writeShort(newServerSendSeq);//sendReq 服务器每次发送消息都会自增
                byteBuf.writeShort(clientSendReq); //recvReq对于服务器来说是客户端的sendReq
            } else {
                byteBuf.writeInt(header.getBodySize());
                byteBuf.writeByte(header.getVersion());
                byteBuf.writeByte(header.getType());
                byteBuf.writeLong(header.getOpTime());
                byteBuf.writeShort(header.getSessionId());
                byteBuf.writeShort(header.getSendSeq());
                byteBuf.writeShort(header.getRecvSeq());
            }
            if (logicBytes != null){
                byteBuf.writeBytes(logicBytes);
            }
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
