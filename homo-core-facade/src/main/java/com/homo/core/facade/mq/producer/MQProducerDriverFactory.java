package com.homo.core.facade.mq.producer;

import com.homo.core.facade.mq.MQSupport;
import org.jetbrains.annotations.NotNull;

/**
 * 生产者驱动创建工厂
 */
public interface MQProducerDriverFactory extends MQSupport {

    @NotNull MQProducerDriver create();
}
