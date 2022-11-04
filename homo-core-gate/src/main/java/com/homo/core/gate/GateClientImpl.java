package com.homo.core.gate;

import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.facade.gate.GateServer;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class GateClientImpl implements GateClient<GateMessage> {
    private final GateServerImpl gateServer;
    private final String name;

    public GateClientImpl(GateServerImpl gateServer, String name) {
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
    public GateServer<GateMessage> getGateServer() {
        return gateServer;
    }

    @Override
    public  void pong(byte[] data) {
        gateServer.pong(this,data);
    }


}
