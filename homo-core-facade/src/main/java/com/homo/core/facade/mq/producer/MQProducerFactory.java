package com.homo.core.facade.mq.producer;

/**
 * 消费者创建工厂
 */
public interface MQProducerFactory {

    /**
     * 通过配置创建
     * @return
     */
    MQProducer create(MQProducerConfig config);
}
