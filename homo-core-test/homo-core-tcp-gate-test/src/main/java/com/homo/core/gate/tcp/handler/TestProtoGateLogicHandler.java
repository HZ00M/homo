package com.homo.core.gate.tcp.handler;

import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateMessage;
import com.homo.core.facade.gate.GateMessageHeader;
import io.homo.proto.client.Msg;
import io.homo.proto.gate.test.TcpMsg;
import io.homo.proto.gate.test.TcpResp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 使用proto与客户端进行通讯
 */
@Slf4j
@Component
public class TestProtoGateLogicHandler extends ProtoGateLogicHandler {

    @Override
    public void doProcess(Msg msg, GateClient gateClient, GateMessageHeader header) throws Exception {
        String msgId = msg.getMsgId();
        /**
         * 读取客户端消息
         */
        TcpMsg tcpMsg = TcpMsg.parseFrom(msg.getMsgContent());
        log.info("LogicHandler msgId {} tcpMsg {}",msgId,tcpMsg);

        Msg.Builder gateMsgResp = Msg.newBuilder();
        TcpResp resp = TcpResp.newBuilder().setParam("tpc测试返回成功").build();
        gateMsgResp.setMsgId("TcpMsg").setMsgContent(resp.toByteString());
        Msg respMsg = gateMsgResp.build();
        /**
         * 给客户端返回一条消息
         */
        gateClient.sendToClient(msgId,respMsg.toByteArray()).start();
    }
}
