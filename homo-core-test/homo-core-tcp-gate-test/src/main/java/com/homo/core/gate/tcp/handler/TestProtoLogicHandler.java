package com.homo.core.gate.tcp.handler;

import com.homo.core.facade.gate.GateClient;
import io.homo.proto.gate.GateMsg;
import io.homo.proto.gate.test.TcpMsg;
import io.homo.proto.gate.test.TcpResp;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;


@Log4j2
@Component
public class TestProtoLogicHandler extends ProtoLogicHandler{

    @Override
    public void process(GateMsg gateMsg, GateClient gateClient) throws Exception {
        String msgId = gateMsg.getMsgId();
        TcpMsg tcpMsg = TcpMsg.parseFrom(gateMsg.getMsgContent());
        log.info("LogicHandler msgId {} tcpMsg {}",msgId,tcpMsg);

        GateMsg.Builder gateMsgResp = GateMsg.newBuilder();
        TcpResp resp = TcpResp.newBuilder().setParam("tpc测试返回成功").build();
        gateMsgResp.setMsgId("TcpMsg").setMsgContent(resp.toByteString());
        GateMsg msg = gateMsgResp.build();
        gateClient.pong(msg.toByteArray());
    }
}
