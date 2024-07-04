package com.homo.core.mq.consumer;

import com.homo.core.configurable.mq.MQCoreProperties;
import com.homo.core.facade.mq.consumer.MQConsumer;
import com.homo.core.facade.mq.consumer.MQConsumerConfig;
import com.homo.core.facade.mq.consumer.MQConsumerFactory;
import com.homo.core.facade.mq.consumer.SinkHandler;
import com.homo.core.utils.module.Module;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

public class MQConsumerFactoryImpl implements MQConsumerFactory {
    private MQCoreProperties mqCoreProperties;

    public  MQConsumerFactoryImpl(MQCoreProperties mqCoreProperties){
        this.mqCoreProperties = mqCoreProperties;
    }


    @Override
    public MQConsumer create(@NotNull MQConsumerConfig config) {
        MQConsumerImpl mqConsumer = new MQConsumerImpl(config);
        if (mqCoreProperties.getAutoStart()){
            mqConsumer.start();
        }
        return mqConsumer;
    }

}
