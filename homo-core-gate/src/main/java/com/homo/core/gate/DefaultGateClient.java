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
        log.info("DefaultGateClient create clientName {} gateServer.name {} gateServer.port {}", name, gateServer.getName(), gateServer.getPort());
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public void onOpen() {
        log.info("DefaultGateClient onOpen  name {}", name);
    }

    @Override
    public void onClose(String reason) {
        log.info("DefaultGateClient onClose name {} reason {}", name, reason);
    }

    @Override
    public GateServer getGateServer() {
        return gateServer;
    }

    @Override
    public Homo<Boolean> sendToClient(String msgId, byte[] msg) {
        return gateServer.sendToClient(this, msgId, msg);
    }

    @Override
    public Homo<Boolean> sendToClient(String msgId, byte[] msg, Short sessionId, Short clientSendSeq, Short confirmServerSendSeq) {
        return gateServer.sendToClient(this, msgId, msg, sessionId, clientSendSeq, confirmServerSendSeq);
    }

    @Override
    public Homo<Boolean> sendToClientComplete(String msgId, byte[] msg) {
        return gateServer.sendToClientComplete(this, msgId, msg);
    }

    @Override
    public Homo<Boolean> sendToClientComplete(String msgId, byte[] msg, Short sessionId, Short clientSendSeq, Short confirmServerSendSeq) {
       return gateServer.sendToClientComplete(this, msgId, msg, sessionId, clientSendSeq, confirmServerSendSeq);
    }


    @Override
    public void close() {
        gateServer.closeClient(this);
    }

}
