package com.homo.core.gate.tcp.handler;

import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateMessage;
import io.homo.proto.client.Msg;
import io.homo.proto.gate.test.TcpMsg;
import io.homo.proto.gate.test.TcpResp;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * 使用proto与客户端进行通讯
 */
@Log4j2
@Component
public class TestProtoLogicHandler extends ProtoLogicHandler{

    @Override
    public void process(Msg msg, GateClient gateClient, GateMessage.Header header) throws Exception {
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
        gateClient.sendToClient(respMsg.toByteArray());
    }
}
