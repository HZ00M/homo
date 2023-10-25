package com.homo.core.gate.tcp;

import com.google.protobuf.GeneratedMessageV3;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.facade.gate.GateMessageHeader;
import com.homo.core.utils.exception.HomoError;
import com.homo.core.utils.serial.ProtoSerializationProcessor;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;

public class GateMessagePackage implements GateMessage<GateMessagePackage> {
    private static ProtoSerializationProcessor processor = new ProtoSerializationProcessor();
    @Getter
    private GateMessageHeader header;
    @Getter
    private byte[] body;

    public GateMessagePackage(GateMessageHeader header, byte[] body) {
        this.header = header;
        this.body = body;
    }

    public GateMessagePackage(byte[] body){
        this.body = body;
        header = new GateMessageHeader();
        int bodySize = body == null ? 0 : body.length;
        header.setBodySize(bodySize);
        header.setOpTime(System.currentTimeMillis());
    }

    public GateMessagePackage(byte[] body, int version, int type, long opTime, short clientSeq, short sendReq, short recvReq) {
        this.header = new GateMessageHeader();
        header.setBodySize(body.length);
        header.setVersion(version);
        header.setType(type);
        header.setOpTime(opTime);
        header.setSessionId(clientSeq);
        header.setSendSeq(sendReq);
        header.setRecvSeq(recvReq);
        this.body = body;
    }

    @Override
    public GateMessageHeader getHeader() {
        return header;
    }


    public static <T> T parse(byte[] playLoad, Class<T> type) throws Exception {
        if (!type.isAssignableFrom(GeneratedMessageV3.class)) {
            throw HomoError.throwError(HomoError.gateError, "MessagePackage parse error type not proto");
        }
        return processor.readValue(playLoad, type);
    }


    @Override
    public byte[] serial() {
        byte[] pageBytes = new byte[getPackageLength()];
        ByteBuf byteBuf = makeBuf();
        writeFullPack(byteBuf);
        byteBuf.readBytes(pageBytes);
        byteBuf.release();
        return pageBytes;
    }




    public void writeFullPack(ByteBuf buf) {
        this.writeHead(buf);
        if (body != null) {
            buf.writeBytes(body);
        }
    }

    public void writeHead(ByteBuf buf) {
        if (body == null){
            buf.writeInt(0);
        }else {
            buf.writeInt(body.length);
        }
        buf.writeByte(header.getVersion());
        buf.writeByte(header.getType());
        buf.writeLong(header.getOpTime());
        buf.writeShort(header.getSessionId());
        buf.writeShort(header.getSendSeq());
        buf.writeShort(header.getRecvSeq());
    }

    public ByteBuf makeBuf() {
        ByteBuf buf = Unpooled.buffer(this.getPackageLength());
        this.writeFullPack(buf);
        return buf;
    }


    public int getPackageLength(){
        return GateMessage.HEAD_LENGTH + body.length;
    }

    //从buffer中读取整个message消息
    public static GateMessagePackage getFullPack(ByteBuf in){
        GateMessageHeader header = getPackHead(in);
        byte[] body = new byte[header.getBodySize()];
        in.readBytes(body);
        return new GateMessagePackage(header,body);

    }

    //字节流解码->PackHead
    public static GateMessageHeader getPackHead(ByteBuf in){
        GateMessageHeader header = new GateMessageHeader();
        header.setBodySize(in.readInt());
        header.setVersion(in.readByte());
        header.setType(in.readByte());
        header.setOpTime(in.readLong());
        header.setSessionId(in.readShort());
        header.setSendSeq(in.readShort());
        header.setRecvSeq(in.readShort());
        return header;
    }

}
