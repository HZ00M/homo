package com.homo.core.mq.kafka.consumer;

import com.homo.core.configurable.mq.MQKafkaProperties;
import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.consumer.MQConsumerDriver;
import com.homo.core.facade.mq.consumer.ReceiverSink;
import com.homo.core.mq.kafka.consumer.woker.ConsumerWorker;
import com.homo.core.mq.kafka.consumer.woker.KafkaConsumerConfirmWorker;
import com.homo.core.mq.kafka.consumer.woker.KafkaConsumerSyncWorker;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.utils.Bytes;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * kafka消费者驱动
 * 一个驱动就是一个消费者组
 * 一个驱动下每个topic由一个消费者worker负责
 */
@Slf4j
public class MQKafkaConsumerDriver implements MQConsumerDriver {
    private Properties properties;
    private MQKafkaProperties mqKafkaProperties;
    public Map<String, ConsumerWorker> consumerWorkers = new HashMap<>();

    public MQKafkaConsumerDriver(MQKafkaProperties mqKafkaProperties,Properties properties) {
        this.mqKafkaProperties = mqKafkaProperties;
        this.properties = properties;
    }

    @Override
    public @NotNull MQType getType() {
        return MQType.KAFKA;
    }

    @Override
    public void subscribe(String topic, ReceiverSink<byte[]> sink) {
        if (haveSubscribe(topic)) {
            throw new RuntimeException("同一个groupId=" + properties.getProperty(ConsumerConfig.GROUP_ID_CONFIG) + "同一个topic=" + topic + "在驱动层只允许订阅一次");
        }
        KafkaConsumer<String, Bytes> consumer = new KafkaConsumer<>(properties);
        ConsumerWorker worker = null;
        if (Boolean.parseBoolean(properties.getProperty(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG))){
            worker = new KafkaConsumerSyncWorker(consumer,topic,sink,mqKafkaProperties.getPollWailMs());
        }else {
            worker = new KafkaConsumerConfirmWorker(consumer,topic,sink,mqKafkaProperties.getPollWailMs(),mqKafkaProperties.getMaxPollRecords());
        }
        consumerWorkers.put(topic, worker);
    }

    @Override
    public boolean haveSubscribe(String topic) {
        return consumerWorkers.containsKey(topic);
    }

    @Override
    public void start() {
        for (ConsumerWorker worker : consumerWorkers.values()) {
            if (worker.getStatus() != ConsumerWorker.Status.RUNNING){
                worker.stop();
            }
        }
    }

    @Override
    public void stop() {
        for (ConsumerWorker worker : consumerWorkers.values()) {
            if (worker.getStatus() != ConsumerWorker.Status.TERMINATED){
                worker.stop();
            }
        }
    }
}
