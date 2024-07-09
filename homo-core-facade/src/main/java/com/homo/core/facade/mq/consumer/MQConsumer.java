package com.homo.core.facade.mq.consumer;

import com.homo.core.facade.mq.MQCodeC;
import com.homo.core.facade.mq.MQSupport;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * 消费者客户端接口，提供接收消息功能
 */
public interface MQConsumer extends MQSupport {


    <T extends Serializable> void addReceiver(@NotNull final String originTopic,@NotNull ReceiverSink<T> sink) throws Exception;


     void addReceiver(@NotNull final SinkHandler sinkHandler) throws Exception;

    <T extends java.io.Serializable> void registerCodec(@NotNull String originTopic, @NotNull MQCodeC<T, byte[]> codec);
    <T extends java.io.Serializable> void registerGlobalCodec(@NotNull MQCodeC<T, byte[]> codec);
    /**
     * 设置错误信息处理器。消费的错误会调用此监听器
     * @param listener
     */
    void addErrorListener(ErrorListener listener);

    Status getStatus();

    void start();

    void stop();

    enum Status {
        INIT,
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED
    }
}
