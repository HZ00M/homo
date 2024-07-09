package com.homo;

import com.alibaba.fastjson.JSONObject;
import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.consumer.*;
import com.homo.core.facade.mq.strategy.TopicResolveStrategyEnum;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.module.ServiceModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class MQConsumerHandler1 implements SinkHandler, ServiceModule {
    @Autowired
    RootModule rootModule;
    @Autowired
    MQConsumerFactory mqConsumerFactory;
    MQConsumer mqConsumer;

    @Override
    public void moduleInit() {
        String appId = rootModule.getServerInfo().getAppId();
        String regionId = rootModule.getServerInfo().getRegionId();
        MQConsumerConfig config = new MQConsumerConfig(MQType.KAFKA, appId, regionId, "group-1", TopicResolveStrategyEnum.APPEND_APP_ID_SERVER_ID);
        mqConsumer = mqConsumerFactory.create(config);
    }

    @Override
    public void afterAllModuleInit() {
        try {
            ReceiverSink<JSONObject> receiverSink2 = (topic, message, callback) -> {
                log.info("MQConsumerHandler1 receiverSink2 topic {} message {} ", topic, message);
                callback.confirm();
            };
            mqConsumer.addReceiver(TopicConstant.topic_1, receiverSink);
            mqConsumer.addReceiver(TopicConstant.topic_1, receiverSink2);
            mqConsumer.addReceiver(this);
            mqConsumer.start();
        } catch (Exception e) {
            log.error("MQConsumerHandler1 afterAllModuleInit error", e);
        }
    }

    @SinkFunc(topics = {TopicConstant.topic_1,"111"})
    public void sinkFun1(String topic, JSONObject message, ConsumerCallback callback) {
        log.info("MQConsumerHandler1 sinkFun1 topic {} message {} ", topic, message);
        callback.confirm();
    }

    @SinkFunc(topics = {TopicConstant.topic_1})
    public void sinkFun2(String topic, JSONObject message, ConsumerCallback callback) {
        log.info("MQConsumerHandler1 sinkFun2 topic {} message {} ", topic, message);
        callback.confirm();
    }

    ReceiverSink<JSONObject> receiverSink = (topic, message, callback) -> {
        log.info("MQConsumerHandler1 receiverSink1 topic {} message {} ", topic, message);
        callback.confirm();
    };

}
