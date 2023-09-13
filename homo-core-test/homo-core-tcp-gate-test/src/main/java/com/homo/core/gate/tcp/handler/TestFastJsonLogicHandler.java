package com.homo.core.gate.tcp.handler;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.gate.GateClient;
import com.homo.core.facade.gate.GateMessage;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

/**
 * 使用fastjson作为通讯协议
 */
@Log4j2
@Component
public class TestFastJsonLogicHandler extends FastJsonLogicHandler {
    @Override
    public void process(JSONObject data, GateClient gateClient, GateMessage.Header header) throws Exception {
        log.info("TestJsonLogicHandler process {}",data);
        /**
         * 这里处理具体业务逻辑
         */
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonKey","json数据返回成功");
        byte[] bytes = serializationProcessor.writeByte(jsonObject);
        /**
         * 返回一条消息给客户端
         */
        gateClient.sendToClient(bytes);
    }

}
