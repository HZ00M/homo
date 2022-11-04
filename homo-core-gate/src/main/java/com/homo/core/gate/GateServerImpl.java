package com.homo.core.gate;

import com.google.protobuf.ByteString;
import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateDriver;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.facade.gate.GateServer;
import io.homo.proto.gate.GateMsg;

public class GateServerImpl implements GateServer<GateMessage> {

    private final GateDriver<GateMessage> gateDriver;
    private final String name;
    private final int port;
    private final GateCommonProperties commonProperties;

    public GateServerImpl(String name, int port, GateDriver<GateMessage> gateDriver, GateCommonProperties commonProperties) {
        this.name = name;
        this.port = port;
        this.gateDriver = gateDriver;
        this.commonProperties = commonProperties;
    }

    @Override
    public GateClient<GateMessage> newClient(String addr, int port) {
        return new GateClientImpl(this, addr + ":" + port);
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
    public void pong(GateClient<GateMessage> gateClient, byte[] data) {
        GateMessage gateMessage = GateMessage.makeMessage(null, data);
        gateDriver.pong(gateClient,gateMessage).start();
    }


    @Override
    public void broadcast(String msgType, byte[] data) {
        GateMsg.Builder builder = GateMsg.newBuilder()
                .setMsgId(msgType);
        if (data != null) {
            builder.setMsgContent(ByteString.copyFrom(data));
        }
        GateMsg msg = builder.build();
        GateMessage gateMessage = GateMessage.makeMessage(null, msg.toByteArray());
        gateDriver.broadcast(gateMessage).start();
    }


    @Override
    public GateDriver<GateMessage> getDriver() {
        return gateDriver;
    }
}
