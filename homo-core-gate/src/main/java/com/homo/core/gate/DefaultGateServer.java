package com.homo.core.gate;

import com.google.protobuf.ByteString;
import com.homo.core.facade.gate.*;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.Msg;
import lombok.Setter;

public class DefaultGateServer implements GateServer {

    @Setter
    private GateDriver gateDriver;
    private final String name;
    private final int port;

    public DefaultGateServer(String name, int port) {
        this.name = name;
        this.port = port;
    }


    @Override
    public DefaultGateClient newClient(String addr, int port) {
        return new DefaultGateClient(this, addr + ":" + port);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public Homo<Boolean>  sendToClient(GateClient gateClient, String msgId, byte[] msg, short sessionId, short sendSeq, short recvSeq) {
        Msg msgProto = Msg.newBuilder().setMsgId(msgId).setMsgContent(ByteString.copyFrom(msg)).build();
        GateMessagePackage gateMessage = new GateMessagePackage(msgProto.toByteArray());
        gateMessage.setSessionId(sessionId);
        gateMessage.setSendSeq(sendSeq);
        gateMessage.setRecvSeq(recvSeq);
        gateMessage.setType(GateMessageType.PROTO.ordinal());//todo待优化
        return gateDriver.sendToClient(gateClient,gateMessage);
    }

    @Override
    public Homo<Boolean>  sendToClient(GateClient gateClient, String msgId, byte[] msg) {
        Msg msgProto = Msg.newBuilder().setMsgId(msgId).setMsgContent(ByteString.copyFrom(msg)).build();
        GateMessagePackage gateMessage = new GateMessagePackage(msgProto.toByteArray());
        gateMessage.setType(GateMessageType.PROTO.ordinal());//todo待优化
        return gateDriver.sendToClient(gateClient,gateMessage);
    }

    @Override
    public Homo<Boolean> sendToClientComplete(GateClient gateClient, String msgId, byte[] msg) {
        Msg msgProto = Msg.newBuilder().setMsgId(msgId).setMsgContent(ByteString.copyFrom(msg)).build();
        GateMessagePackage gateMessage = new GateMessagePackage(msgProto.toByteArray());
        gateMessage.setType(GateMessageType.PROTO.ordinal());//todo待优化
        return gateDriver.sendToClientComplete(gateClient,gateMessage);
    }

    @Override
    public Homo<Boolean> sendToClientComplete(GateClient gateClient, String msgId, byte[] msg, short sessionId, short sendSeq, short recvSeq) {
        Msg msgProto = Msg.newBuilder().setMsgId(msgId).setMsgContent(ByteString.copyFrom(msg)).build();
        GateMessagePackage gateMessage = new GateMessagePackage(msgProto.toByteArray());
        gateMessage.setSessionId(sessionId);
        gateMessage.setSendSeq(sendSeq);
        gateMessage.setRecvSeq(recvSeq);
        gateMessage.setType(GateMessageType.PROTO.ordinal());//todo待优化
        return gateDriver.sendToClientComplete(gateClient,gateMessage);
    }


    @Override
    public void broadcast(String msgType, byte[] msgBytes) {
        Msg.Builder builder = Msg.newBuilder()
                .setMsgId(msgType);
        if (msgBytes != null) {
            builder.setMsgContent(ByteString.copyFrom(msgBytes));
        }
        Msg msg = builder.build();
        GateMessagePackage gateMessage = new GateMessagePackage(msg.toByteArray());
        gateDriver.broadcast(gateMessage).start();
    }


    @Override
    public GateDriver getDriver() {
        return gateDriver;
    }

    @Override
    public void setDriver(GateDriver gateDriver) {
        this.gateDriver = gateDriver;
    }

    @Override
    public void closeClient(GateClient gateClient) {
        gateDriver.closeGateClient(gateClient);
    }
}
