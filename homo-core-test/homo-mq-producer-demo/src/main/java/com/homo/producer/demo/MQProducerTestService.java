package com.homo.producer.demo;

import com.alibaba.fastjson.JSONObject;
import com.homo.mq.common.TopicConstant;
import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.producer.MQProducer;
import com.homo.core.facade.mq.producer.MQProducerConfig;
import com.homo.core.facade.mq.producer.MQProducerFactory;
import com.homo.core.facade.mq.producer.ProducerCallback;
import com.homo.core.facade.mq.strategy.TopicResolveStrategyEnum;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.module.ServiceModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class MQProducerTestService implements ServiceModule {
    @Autowired
    @Lazy  //需要将依赖加入lazy，否则这个bean会提前创建  会影响HomoLogHandler的最优先创建
    MQProducerFactory mqProducerFactory;
    @Autowired
    @Lazy
    RootModule rootModule;
    MQProducer mqProducer;

    @Override
    public void moduleInit() {

    }

    @Override
    public void afterAllModuleInit() {
        String appId = rootModule.getServerInfo().getAppId();
        String regionId = rootModule.getServerInfo().getRegionId();
        MQProducerConfig config = new MQProducerConfig(MQType.KAFKA, appId, regionId, TopicResolveStrategyEnum.APPEND_APP_ID_SERVER_ID);
        mqProducer = mqProducerFactory.create(config);
        Thread thread = new Thread(() -> {
            int sendCount = 1;
            JSONObject jsonObject = null;
            while (true) {
                try {
                    jsonObject = new JSONObject();
                    jsonObject.put("topic", TopicConstant.topic_1);
                    jsonObject.put("sendCount", sendCount);
                    log.info("send sendCount {} jsonObject {}", sendCount, jsonObject);
                    mqProducer.send(TopicConstant.topic_1, "key" + sendCount, jsonObject, new ProducerCallback() {
                        @Override
                        public void onCompletion(boolean ok, Throwable throwable) {
                            log.info("onCompletion ret {}", ok, throwable);
                        }
                    });
                    sendCount++;
                    Thread.sleep(10000);
                } catch (Exception e) {
                    log.error("mqProducer.send json error topic {} sendCount {} jsonObject {}", TopicConstant.topic_1, sendCount, jsonObject);
                }
            }
        });
        thread.start();

    }


}
