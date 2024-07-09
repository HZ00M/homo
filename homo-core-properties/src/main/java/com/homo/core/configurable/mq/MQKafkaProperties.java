package com.homo.core.configurable.mq;

import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
public class MQKafkaProperties {
    @Value("${homo.mq.kafka.bootstrap.servers:localhost:9092}")
    private String servers;
    @Value("${homo.mq.kafka.acks:all}")
    private String acks;
    @Value("${homo.mq.kafka.retries:3}")
    private Integer retries;
    @Value("${homo.mq.kafka.retry.backoff.ms:100}")
    private Integer retryBackoffMs;
    @Value("${homo.mq.kafka.delivery.timeout.ms:120000}")
    private Integer deliveryTimeoutMs;
    @Value("${homo.mq.kafka.buffer.memory:33554432}") //32 * 1024 * 1024L 32mb
    private Long bufferMemory;
    @Value("${homo.mq.kafka.batch.size:16384}")
    private Integer batchSize;
    @Value("${homo.mq.kafka.linger.ms:100}")
    private Integer lingerMs;
    @Value("${homo.mq.kafka.producer.queue.capacity:50000}")
    private Integer producerQueueCapacity;
    @Value("${homo.mq.kafka.producer.pool.core.size:16}")
    private Integer producerPoolCoreSize;
    @Value("${homo.mq.kafka.producer.pool.max.size:16}")
    private Integer producerPoolMaxSize;
    @Value("${homo.mq.kafka.producer.pool.keepLive.second:60}")
    private Integer producerPoolKeepLive;
    @Value("${homo.mq.kafka.consumer.max.poll.records.size:60}")
    private Integer maxPollRecords;
    @Value("${homo.mq.kafka.consumer.auto.commit:false}")
    private Boolean autoCommit;
    @Value("${homo.mq.kafka.consumer.poll.wait.millisecond:2000}")
    private long pollWailMs;
    @Value("${homo.mq.kafka.key.deserializer:org.apache.kafka.common.serialization.StringDeserializer}")
    private String keyDeserializer;
    @Value("${homo.mq.kafka.value.deserializer:org.apache.kafka.common.serialization.BytesDeserializer}")
    private String valueDeserializer;

}
