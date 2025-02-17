package com.homo.mq.common;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.mq.MQCodeC;


public class JsonCodec implements MQCodeC<JSONObject, byte[]> {
    @Override
    public byte[] encode(JSONObject jsonObject) throws Exception {
        return JSONObject.toJSONBytes(jsonObject);
    }

    @Override
    public JSONObject decode(byte[] bytes) throws Exception {
        return JSONObject.parseObject(bytes,JSONObject.class);
    }
}
