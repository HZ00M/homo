package com.homo.core.configurable.mq;

import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
public class MQKafkaProperties {

    // Kafka 服务器地址列表，多个地址用逗号分隔，默认 localhost:9092
    @Value("${homo.mq.kafka.bootstrap.servers:localhost:9092}")
    private String servers;

    // Kafka 确认模式："all" 表示所有副本都确认接收，确保消息持久化
    @Value("${homo.mq.kafka.acks:all}")
    private String acks;

    // Kafka 生产者重试次数，默认 3 次
    @Value("${homo.mq.kafka.retries:3}")
    private Integer retries;

    // Kafka 生产者重试的时间间隔（毫秒），避免频繁重试导致服务器过载
    @Value("${homo.mq.kafka.retry.backoff.ms:100}")
    private Integer retryBackoffMs;

    // 生产者交付消息的超时时间（毫秒），包括所有重试时间，默认 120000ms (2分钟)
    @Value("${homo.mq.kafka.delivery.timeout.ms:120000}")
    private Integer deliveryTimeoutMs;

    // Kafka 缓冲区内存大小，用于缓存未发送的消息（字节），默认 32 MB
    @Value("${homo.mq.kafka.buffer.memory:33554432}")
    private Long bufferMemory;

    // 批量发送的消息总字节大小（字节），此处默认 16 KB
    @Value("${homo.mq.kafka.batch.size:16384}")
    private Integer batchSize;

    // 批量发送的等待时间（毫秒），在未达到批量大小时等待该时间后发送消息，默认 100 ms
    @Value("${homo.mq.kafka.linger.ms:100}")
    private Integer lingerMs;

    // Kafka 生产者队列容量，控制待发送消息的队列长度，默认 50000 条
    @Value("${homo.mq.kafka.producer.queue.capacity:50000}")
    private Integer producerQueueCapacity;

    // 生产者线程池核心线程数，默认 16
    @Value("${homo.mq.kafka.producer.pool.core.size:16}")
    private Integer producerPoolCoreSize;

    // 生产者线程池最大线程数，默认 16
    @Value("${homo.mq.kafka.producer.pool.max.size:16}")
    private Integer producerPoolMaxSize;

    // 生产者线程池空闲线程存活时间（秒），默认 60 秒
    @Value("${homo.mq.kafka.producer.pool.keepLive.second:60}")
    private Integer producerPoolKeepLive;

    // Kafka 消费者每次轮询最大拉取记录数，默认 60 条
    @Value("${homo.mq.kafka.consumer.max.poll.records.size:60}")
    private Integer maxPollRecords;

    // Kafka 消费者是否启用自动提交偏移量，默认 false（需要手动提交）
    @Value("${homo.mq.kafka.consumer.auto.commit:false}")
    private Boolean autoCommit;

    // Kafka 消费者拉取消息的最大等待时间（毫秒），默认 2000 ms
    @Value("${homo.mq.kafka.consumer.poll.wait.millisecond:2000}")
    private long pollWailMs;

    // Kafka 消费者使用的 Key 反序列化器类，默认 StringDeserializer
    @Value("${homo.mq.kafka.key.deserializer:org.apache.kafka.common.serialization.StringDeserializer}")
    private String keyDeserializer;

    // Kafka 消费者使用的 Value 反序列化器类，默认 BytesDeserializer
    @Value("${homo.mq.kafka.value.deserializer:org.apache.kafka.common.serialization.BytesDeserializer}")
    private String valueDeserializer;
}

