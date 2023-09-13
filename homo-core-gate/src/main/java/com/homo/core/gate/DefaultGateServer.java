package com.homo.core.gate;

import com.google.protobuf.ByteString;
import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateDriver;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.facade.gate.GateServer;
import com.homo.core.utils.rector.Homo;
import io.homo.proto.client.Msg;
import lombok.Setter;

public class DefaultGateServer implements GateServer<DefaultGateClient> {

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
    public void sendToClient(GateClient gateClient, byte[] data) {
        GateMessage gateMessage = GateMessage.makeMessage(null, data);
        gateDriver.sendToclient(gateClient,gateMessage).start();
    }

    @Override
    public Homo<Boolean> sendToClientComplete(GateClient gateClient, byte[] data) {
        GateMessage gateMessage = GateMessage.makeMessage(null, data);
        return gateDriver.sendToclient(gateClient,gateMessage);
    }


    @Override
    public void broadcast(String msgType, byte[] data) {
        Msg.Builder builder = Msg.newBuilder()
                .setMsgId(msgType);
        if (data != null) {
            builder.setMsgContent(ByteString.copyFrom(data));
        }
        Msg msg = builder.build();
        GateMessage gateMessage = GateMessage.makeMessage(null, msg.toByteArray());
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
