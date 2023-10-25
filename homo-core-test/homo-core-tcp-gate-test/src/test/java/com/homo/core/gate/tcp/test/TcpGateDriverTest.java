package com.homo.core.gate.tcp.test;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.facade.gate.GateMessageHeader;
import com.homo.core.gate.tcp.GateMessagePackage;
import com.homo.core.gate.tcp.GateMessageType;
import com.homo.core.gate.tcp.TcpGateServerApplication;
import com.homo.core.utils.serial.FastjsonSerializationProcessor;
import io.homo.proto.client.Msg;
import io.homo.proto.gate.test.TcpMsg;
import io.homo.proto.gate.test.TcpResp;
import io.netty.buffer.ByteBuf;
import io.netty.channel.PreferHeapByteBufAllocator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

@Slf4j
@SpringBootTest(classes = TcpGateServerApplication.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class TcpGateDriverTest {


    @Test
    public void testProtoTcp() throws InterruptedException {
        Msg.Builder builder = Msg.newBuilder();
        TcpMsg tcpMsg = TcpMsg.newBuilder().setParam("test").build();
        builder.setMsgId("TcpMsg");
        builder.setMsgContent(tcpMsg.toByteString());
        Msg gateMsg = builder.build();
        byte[] body = gateMsg.toByteArray();
        GateMessageHeader header = new GateMessageHeader();
        header.setBodySize(body.length);
        header.setVersion(1);
        header.setType(GateMessageType.PROTO.ordinal());
        header.setOpTime(System.currentTimeMillis());
        header.setSessionId(Short.MIN_VALUE);
        header.setSendSeq(Short.MIN_VALUE);
        header.setRecvSeq(Short.MIN_VALUE);
        GateMessagePackage gateMessagePackage = new GateMessagePackage(header, body);
        try {
            //1、创建客户端的Socket对象(Socket)
            Socket socket = new Socket("127.0.0.1", 30033);
            //2、获取输出流，写数据
            OutputStream os = socket.getOutputStream();//返回此套接字的输出流
            os.write(gateMessagePackage.serial());
            //3、释放资源
            //socket.close();
            //获取输入流，读取数据
            InputStream inputStream = socket.getInputStream();
            BufferedInputStream stream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int read;

            while ((read = stream.read(buffer)) != -1) {
                log.info("reading proto msg start");
                byte array[]=new byte [read];
                for (int i =0;i< array.length;i++) {
                    array[i] = buffer[i];
                }
                ByteBuf buf = PreferHeapByteBufAllocator.DEFAULT.heapBuffer();
                buf.writeBytes(array);
                int bodySize = buf.readInt();
                int version = buf.readByte() & 0xff;
                int packType = buf.readByte() & 0xff;
                long opTime = buf.readLong();
                short sessionId = buf.readShort();
                short opSeq = buf.readShort();
                //ByteBuf.array() 只有 HEAP BUFFER (堆缓冲区)可以使用
                byte[] respBytes = new byte[buf.readableBytes()];
                buf.readBytes(respBytes);
                Msg respGateMsg = Msg.parseFrom(respBytes);
                String msgId = respGateMsg.getMsgId();
                TcpResp resp = TcpResp.parseFrom(respGateMsg.getMsgContent());
                log.info("reading proto msg done msgId {} resp {}",msgId,new String(resp.getParam().getBytes(StandardCharsets.UTF_8)));
                break;
            }

        } catch (Exception e) {
            log.error("tcp connect error ", e);
        }
        log.info("testProtoTcp finish ");
    }


    @Test
    public void testJsonTcp() throws InterruptedException {
        FastjsonSerializationProcessor processor = new FastjsonSerializationProcessor();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("test1", "1");
        jsonObject.put("test2", "2");
        byte[] body = processor.writeByte(jsonObject);
        GateMessageHeader header = new GateMessageHeader();
        header.setBodySize(body.length);
        header.setVersion(1);
        header.setType(GateMessageType.JSON.ordinal());
        header.setOpTime(System.currentTimeMillis());
        header.setSessionId(Short.MIN_VALUE);
        header.setSendSeq(Short.MIN_VALUE);
        GateMessagePackage gateMessagePackage = new GateMessagePackage(header, body);
        try {
            //1、创建客户端的Socket对象(Socket)
            Socket socket = new Socket("127.0.0.1", 30033);
            //2、获取输出流，写数据
            OutputStream os = socket.getOutputStream();//返回此套接字的输出流
            os.write(gateMessagePackage.serial());
            //3、释放资源
            //socket.close();
            //获取输入流，读取数据
            InputStream inputStream = socket.getInputStream();
            BufferedInputStream stream = new BufferedInputStream(inputStream);
            byte[] buffer = new byte[1024];
            int read;

            while ((read = stream.read(buffer)) != -1) {
                log.info("reading json msg start");
                byte array[]=new byte [read];
                for (int i =0;i< array.length;i++) {
                    array[i] = buffer[i];
                }
                ByteBuf buf = PreferHeapByteBufAllocator.DEFAULT.heapBuffer();
                buf.writeBytes(array);
                int bodySize = buf.readInt();
                int version = buf.readByte() & 0xff;
                int packType = buf.readByte() & 0xff;
                long opTime = buf.readLong();
                short sessionId = buf.readShort();
                short opSeq = buf.readShort();
                //ByteBuf.array() 只有 HEAP BUFFER (堆缓冲区)可以使用
                byte[] respBytes = new byte[buf.readableBytes()];
                buf.readBytes(respBytes);
                JSONObject resp = processor.readValue(respBytes, JSONObject.class);
                log.info("reading json msg done resp {}",resp);
                break;
            }
        } catch (Exception e) {
            log.error("tcp connect error ", e);
        }
        log.info("testJsonTcp finish ");
    }
}
