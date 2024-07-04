package com.homo.core.facade.mq.consumer;

import org.jetbrains.annotations.NotNull;

/**
 * 消费者创建工厂接口
 */
public interface MQConsumerFactory {

    /**
     * 通过MQConsumerConfig中的配置创建消费者。
     * @param config
     * @return
     */
    MQConsumer create(@NotNull MQConsumerConfig config) ;
}
