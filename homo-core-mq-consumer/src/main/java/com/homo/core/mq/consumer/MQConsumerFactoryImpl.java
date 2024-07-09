package com.homo.core.mq.consumer;

import com.homo.core.facade.mq.consumer.MQConsumer;
import com.homo.core.facade.mq.consumer.MQConsumerConfig;
import com.homo.core.facade.mq.consumer.MQConsumerFactory;
import org.jetbrains.annotations.NotNull;

public class MQConsumerFactoryImpl implements MQConsumerFactory{
    public MQConsumerFactoryImpl() {
    }


    @Override
    public MQConsumer create(@NotNull MQConsumerConfig config) {
        MQConsumerImpl mqConsumer = new MQConsumerImpl(config);
        return mqConsumer;
    }
}
