package com.homo.core.mq.producer;

import com.homo.core.facade.mq.MQCodeC;
import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.producer.*;
import com.homo.core.mq.base.ByteSrcCodecRegister;
import com.homo.core.mq.base.MQDriverFactoryProvider;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class MQProducerImpl implements MQProducer {
    MQProducerConfig config;
    MQProducerDriver driver;
    Map<String, String> realTopics;
    ByteSrcCodecRegister codecRegister;

    public MQProducerImpl(@NotNull MQProducerConfig config) {
        MQType mqType = config.getType();
        MQProducerDriverFactory driverFactory = MQDriverFactoryProvider.getProducerDriverFactory(mqType);
        if (driverFactory == null) {
            throw new RuntimeException(String.format("no producer driver implementation found for MQType %s !", mqType));
        } else {
            log.info(String.format("find the producer driver implementation for MQType %s .", mqType));
        }
        this.config = config;
        this.driver = driverFactory.create();
        this.codecRegister = new ByteSrcCodecRegister();
        this.realTopics = new ConcurrentHashMap<>();
    }

    @Override
    public @NotNull MQType getType() {
        return driver.getType();
    }

    @Override
    public @NotNull String getRealTopic(@NotNull String originTopic) {
        return realTopics.computeIfAbsent(originTopic, (_topic) -> {
            return config.getTopicResolveStrategy().getRealTopic(_topic, config.getAppId(), config.getRegionId());
        });
    }

    @Override
    public <T extends Serializable> void send(@NotNull String originTopic, @NotNull T message) throws Exception {
        String realTopic = getRealTopic(originTopic);
        driver.sendMessage(realTopic, encodeMessage(realTopic, message));
    }

    @Override
    public <T extends Serializable> void send(@NotNull String originTopic, @NotNull T message, @NotNull ProducerCallback callback) throws Exception {
        String realTopic = getRealTopic(originTopic);
        driver.sendMessage(realTopic, encodeMessage(realTopic, message), callback);
    }

    @Override
    public void close() throws Exception {
        driver.close();
    }

    protected <T extends Serializable> byte[] encodeMessage(@NotNull String realTopic, @NotNull T message) throws Exception {
        MQCodeC<Serializable, byte[]> codec = codecRegister.getCodec(realTopic);
        return codec.encode(message);
    }

    public @NotNull Map<String, MQCodeC<?, byte[]>> getCodeCs() {
        return codecRegister.getCodecs();
    }

    public <T extends Serializable> void setCodec(@NotNull String originTopic, @NotNull MQCodeC<T, byte[]> codec) {
        String realTopic = this.getRealTopic(originTopic);
        codecRegister.setCodec(realTopic, codec);
    }
}
