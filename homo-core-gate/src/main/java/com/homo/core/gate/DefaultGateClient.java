package com.homo.core.gate;

import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateServer;
import com.homo.core.utils.rector.Homo;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DefaultGateClient implements GateClient {
    private final GateServer gateServer;
    private final String name;


    public DefaultGateClient(GateServer gateServer, String name) {
        this.gateServer = gateServer;
        this.name = name;
    }

    @Override
    public void onOpen() {
        log.info("GateClientImpl {} onOpen ", name);
    }

    @Override
    public void onClose(String reason) {
        log.info("GateClientImpl onClose reason {}", reason);
    }

    @Override
    public GateServer getGateServer() {
        return gateServer;
    }

    @Override
    public  void sendToClient(byte[] data) {
        gateServer.sendToClient(this,data);
    }

    @Override
    public Homo<Boolean> sendToClientComplete(byte[] data) {
       return gateServer.sendToClientComplete(this,data);
    }

    @Override
    public void close() {
        gateServer.closeClient(this);
    }

}
