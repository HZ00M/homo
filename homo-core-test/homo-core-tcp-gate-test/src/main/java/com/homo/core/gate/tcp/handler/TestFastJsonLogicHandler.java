package com.homo.core.gate.tcp.handler;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.gate.GateClient;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class TestFastJsonLogicHandler extends FastJsonLogicHandler {
    @Override
    public void process(JSONObject data, GateClient gateClient) throws Exception {
        log.info("TestJsonLogicHandler process {}",data);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonKey","json数据返回成功");
        byte[] bytes = serializationProcessor.writeByte(jsonObject);
        gateClient.pong(bytes);
    }
}
