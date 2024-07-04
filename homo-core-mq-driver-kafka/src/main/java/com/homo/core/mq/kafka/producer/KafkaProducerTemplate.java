package com.homo.core.mq.kafka.producer;

import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * kafka生产者封装类，通过线程池发送，不会阻塞主线程. 但是任务积压数量超过DEFAULT_QUEUE_CAPACITY的值（50000）则会抛出异常，调用方需要捕捉此异常
 */
public class KafkaProducerTemplate<K, V> implements AutoCloseable {
    protected Properties properties;
    protected KafkaProducer<K, V> producer;
    protected ThreadPoolExecutor sendExecutor;

    public KafkaProducerTemplate(Properties properties, ThreadPoolExecutor executor) {
        this.properties = Objects.requireNonNull(properties, "properties is null");
        this.sendExecutor = Objects.requireNonNull(executor, "executor not allow null");
        this.producer = new KafkaProducer<K, V>(properties);
    }

    public void sendMessage(String topic, V msg) {
        sendMessage(topic, null, msg, null);
    }

    public void sendMessage(String topic, K key, V msg) {
        sendMessage(topic, key, msg, null);
    }

    public void sendMessage(String topic, K key, V msg, Callback callback) {
        sendExecutor.submit(() -> {
            ProducerRecord<K, V> record;
            if (key == null) {
                record = new ProducerRecord<>(topic, msg);
            } else {
                record = new ProducerRecord<>(topic, key, msg);
            }
            return producer.send(record, callback);
        });
    }

    @Override
    public void close() throws Exception {
        producer.close();
    }
}
