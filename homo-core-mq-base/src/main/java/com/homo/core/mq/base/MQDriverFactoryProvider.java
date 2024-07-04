package com.homo.core.mq.base;

import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.consumer.MQConsumerDriverFactory;
import com.homo.core.facade.mq.producer.MQProducerDriverFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 生产者驱动管理器。通过SPI功能，自动注入不同的驱动实现
 */
@Slf4j
public class MQDriverFactoryProvider {
    public static final Map<MQType, MQProducerDriverFactory> producerFactoryMap = new ConcurrentHashMap<>();
    public static final Map<MQType, MQConsumerDriverFactory> consumerFactoryMap = new ConcurrentHashMap<>();
    private MQDriverFactoryProvider(){}

//    private static void inject(){
//        ServiceLoader<MQProducerDriverFactory> factories = ServiceLoader.load(MQProducerDriverFactory.class);
//        for (MQProducerDriverFactory factory : factories) {
//            if (factoryMap.containsKey(factory.getType())){
//                throw new RuntimeException(String.format("inject scan find MQType repeat %s,Each MQType can only rely on one driver",factory.getType()));
//            }
//            factoryMap.put(factory.getType(),factory);
//        }
//    }
    public static MQConsumerDriverFactory getConsumerDriverFactory(MQType mqType){
        return consumerFactoryMap.get(mqType);
    }
    public static MQProducerDriverFactory getProducerDriverFactory(MQType mqType){
        return producerFactoryMap.get(mqType);
    }
}
