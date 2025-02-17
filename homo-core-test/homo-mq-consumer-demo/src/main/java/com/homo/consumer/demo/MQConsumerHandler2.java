package com.homo.consumer.demo;

import com.alibaba.fastjson.JSONObject;
import com.homo.mq.common.TopicConstant;
import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.consumer.*;
import com.homo.core.facade.mq.strategy.TopicResolveStrategyEnum;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.module.ServiceModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQConsumerHandler2 implements SinkHandler,ServiceModule {
    @Autowired
    RootModule rootModule;
    @Autowired
    MQConsumerFactory mqConsumerFactory;
    MQConsumer mqConsumer;
    @Override
    public void moduleInit() {
        String appId = rootModule.getServerInfo().getAppId();
        String regionId = rootModule.getServerInfo().getRegionId();
        MQConsumerConfig config = new MQConsumerConfig(MQType.KAFKA, appId, regionId, "group-2", TopicResolveStrategyEnum.APPEND_APP_ID_SERVER_ID);
        mqConsumer = mqConsumerFactory.create(config);
    }

    @Override
    public void afterAllModuleInit() {
        try {
            ReceiverSink<JSONObject> receiverSink = new ReceiverSink<JSONObject>() {
                @Override
                public void onSink(String topic, JSONObject message, ConsumerCallback callback) {
                    log.info("MQConsumerHandler2 receiverSink topic {} message {} ", topic, message);
                    callback.confirm();
                }
            };
            mqConsumer.addReceiver(TopicConstant.topic_1, receiverSink);
            mqConsumer.addReceiver(this);
            mqConsumer.start();
        } catch (Exception e) {
            log.error("MQConsumerHandler2 afterAllModuleInit error", e);
        }
    }

    @SinkFunc(topics = {TopicConstant.topic_1})
    public void sinkFun1(String topic, JSONObject message, ConsumerCallback callback) {
        log.info("MQConsumerHandler2 sinkFun1 topic {} message {} ", topic, message);
        callback.confirm();
    }

    @SinkFunc(topics = {TopicConstant.topic_1})
    public void sinkFun2(String topic, JSONObject message, ConsumerCallback callback) {
        log.info("MQConsumerHandler2 sinkFun2 topic {} message {} ", topic, message);
        callback.confirm();
    }



}
