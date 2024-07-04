package com.homo.core.mq.producer;

import com.homo.core.facade.mq.producer.MQProducer;
import com.homo.core.facade.mq.producer.MQProducerConfig;
import com.homo.core.facade.mq.producer.MQProducerFactory;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MQProducerFactoryImpl implements MQProducerFactory {

    @Override
    public MQProducer create(MQProducerConfig config) {
        MQProducer mqProducer = new MQProducerImpl(config);
        return mqProducer;
    }

}
