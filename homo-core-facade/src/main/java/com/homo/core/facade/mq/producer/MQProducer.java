package com.homo.core.facade.mq.producer;

import com.homo.core.facade.mq.MQCodeC;
import com.homo.core.facade.mq.MQSupport;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

/**
 * 生产者客户端接口，提供发送消息功能
 */
public interface MQProducer extends AutoCloseable, MQSupport {

    /**
     * 通过topic组装返回真实的topic名称
     * @param originTopic
     * @return
     */
    @NotNull String getRealTopic(@NotNull String originTopic);
    <T extends Serializable> void send(@NotNull final String originTopic, @NotNull final T message)throws Exception;
    <T extends Serializable> void send(@NotNull final String originTopic,@NotNull final String key, @NotNull final T message)throws Exception;
    <T extends Serializable> void send(@NotNull final String originTopic, @NotNull final T message, @NotNull final ProducerCallback callback)throws Exception;
    <T extends Serializable> void send(@NotNull final String originTopic, final String key, @NotNull final T message,final ProducerCallback callback) throws Exception;
    <T extends java.io.Serializable> void registerCodec(@NotNull String originTopic, @NotNull MQCodeC<T, byte[]> codec);
    <T extends java.io.Serializable> void registerGlobalCodec(@NotNull MQCodeC<T, byte[]> codec);
}
