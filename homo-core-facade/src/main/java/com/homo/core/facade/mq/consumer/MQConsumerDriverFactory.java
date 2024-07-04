package com.homo.core.facade.mq.consumer;

import com.homo.core.facade.mq.MQSupport;
import org.jetbrains.annotations.NotNull;

/**
 * 消费者驱动创建工厂
 */
public interface MQConsumerDriverFactory extends MQSupport {

    @NotNull MQConsumerDriver create(String group);
}
