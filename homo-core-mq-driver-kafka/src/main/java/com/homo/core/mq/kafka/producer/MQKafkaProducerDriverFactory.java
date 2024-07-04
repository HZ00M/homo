package com.homo.core.mq.kafka.producer;

import com.homo.core.configurable.mq.MQKafkaProperties;
import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.producer.MQProducerConfig;
import com.homo.core.facade.mq.producer.MQProducerDriver;
import com.homo.core.facade.mq.producer.MQProducerDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
public class MQKafkaProducerDriverFactory implements MQProducerDriverFactory {

    MQKafkaProperties mqKafkaProperties;
    private Properties properties;
    private ThreadPoolExecutor executor;

    public MQKafkaProducerDriverFactory(MQKafkaProperties mqKafkaProperties) {
        this.mqKafkaProperties = mqKafkaProperties;
        this.properties = buildKafkaProperties();
        this.executor = buildThreadPoolExecutor();
    }


    @Override
    public @NotNull MQType getType() {
        return MQType.KAFKA;
    }

    @Override
    public @NotNull MQProducerDriver create() {
        return new MQKafkaProducerDriver(properties, executor);
    }

    public Properties buildKafkaProperties() {
        Properties properties = new Properties();
        properties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, mqKafkaProperties.getServers());
        properties.put(ProducerConfig.ACKS_CONFIG, mqKafkaProperties.getAcks());
        properties.put(ProducerConfig.RETRIES_CONFIG, mqKafkaProperties.getRetries());
        properties.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, mqKafkaProperties.getRetryBackoffMs());
        properties.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, mqKafkaProperties.getDeliveryTimeoutMs());
        properties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, mqKafkaProperties.getBufferMemory());
        properties.put(ProducerConfig.BATCH_SIZE_CONFIG, mqKafkaProperties.getBatchSize());
        properties.put(ProducerConfig.LINGER_MS_CONFIG, mqKafkaProperties.getLingerMs());
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class.getName());
        return properties;
    }

    public ThreadPoolExecutor buildThreadPoolExecutor() {
        int cpu = Runtime.getRuntime().availableProcessors();
        return new ThreadPoolExecutor(mqKafkaProperties.getProducerPoolCoreSize(),
                mqKafkaProperties.getProducerPoolMaxSize(),
                mqKafkaProperties.getProducerPoolKeepLive(), TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(mqKafkaProperties.getProducerQueueCapacity()), new ThreadPoolExecutor.AbortPolicy());
    }
}
