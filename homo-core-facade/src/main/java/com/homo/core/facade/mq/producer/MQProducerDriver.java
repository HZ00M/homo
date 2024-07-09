package com.homo.core.facade.mq.producer;

import com.homo.core.facade.mq.MQSupport;
import org.jetbrains.annotations.NotNull;

public interface MQProducerDriver extends AutoCloseable,MQSupport {
    void sendMessage(@NotNull String topic,String key, byte @NotNull [] message,  ProducerCallback callback);
}
