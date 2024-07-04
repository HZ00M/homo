package com.homo.core.facade.mq.producer;

import com.homo.core.facade.mq.MQSupport;
import com.homo.core.facade.mq.producer.ProducerCallback;
import org.jetbrains.annotations.NotNull;

public interface MQProducerDriver extends AutoCloseable,MQSupport {
    void sendMessage(@NotNull String topic, byte @NotNull [] message);
    void sendMessage(@NotNull String topic, byte @NotNull [] message, @NotNull ProducerCallback callback);
    void sendMessage(@NotNull String topic,String key, byte @NotNull [] message, @NotNull ProducerCallback callback);
}
