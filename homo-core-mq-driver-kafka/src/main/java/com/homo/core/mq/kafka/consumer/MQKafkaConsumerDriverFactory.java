package com.homo.core.mq.kafka.consumer;

import com.homo.core.configurable.mq.MQKafkaProperties;
import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.consumer.MQConsumerDriver;
import com.homo.core.facade.mq.consumer.MQConsumerDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;

@Slf4j
public class MQKafkaConsumerDriverFactory implements MQConsumerDriverFactory {
    MQKafkaProperties mqKafkaProperties;
    private Properties prototype;

    public MQKafkaConsumerDriverFactory(MQKafkaProperties mqKafkaProperties){
        this.mqKafkaProperties = mqKafkaProperties;
        this.prototype = buildKafkaProperties();
    }

    @Override
    public @NotNull MQType getType() {
        return MQType.KAFKA;
    }

    @Override
    public @NotNull MQConsumerDriver create(String group) {
        Properties consumerProperties = new Properties(prototype);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG,group);
        return new MQKafkaConsumerDriver(mqKafkaProperties,consumerProperties);
    }

    public Properties buildKafkaProperties() {
        Properties properties = new Properties();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, mqKafkaProperties.getServers());
        properties.put(ConsumerConfig.RETRY_BACKOFF_MS_CONFIG, mqKafkaProperties.getRetryBackoffMs());
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG,mqKafkaProperties.getMaxPollRecords());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,mqKafkaProperties.getAutoCommit());
        return properties;
    }
}
