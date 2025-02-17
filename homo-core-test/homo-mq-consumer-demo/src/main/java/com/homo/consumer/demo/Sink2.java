package com.homo.consumer.demo;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.mq.consumer.ConsumerCallback;
import com.homo.core.facade.mq.consumer.ReceiverSink;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Sink2 implements ReceiverSink<JSONObject> {
    @Override
    public void onSink(String topic, JSONObject message, ConsumerCallback callback) {
        log.info("MQConsumerHandler2 receiverSink topic {} message {} ", topic, message);
        callback.confirm();
    }
}
