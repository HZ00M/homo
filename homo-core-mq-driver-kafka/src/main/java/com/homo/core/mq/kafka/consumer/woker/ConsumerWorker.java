package com.homo.core.mq.kafka.consumer.woker;

import com.homo.core.facade.mq.consumer.ReceiverSink;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.utils.Bytes;

@Slf4j
public abstract class ConsumerWorker {
    protected String name;
    protected String topic;
    protected KafkaConsumer<String, Bytes> consumer;
    protected ReceiverSink<byte[]> sink;
    protected Status status;
    private Thread workThread = null;

    public ConsumerWorker(String name,KafkaConsumer<String, Bytes> consumer, String topic, ReceiverSink<byte[]> sink) {
        this.name = name;
        this.consumer = consumer;
        this.topic = topic;
        this.sink = sink;
        this.status = Status.INIT;
    }

    public abstract void process();

    //启动消费
    public synchronized void start() {
        if (status.equals(Status.RUNNING)) {
            throw new RuntimeException("already started");
        }
        status = Status.RUNNING;
        workThread = new Thread(this::process);
        workThread.start();
        log.info("ConsumerWorker name {} start topic {} status {}",name, topic, status.name());
    }

    public synchronized void stop() {
        if (status.equals(Status.TERMINATED)) {
            throw new RuntimeException("already terminated");
        }
        status = Status.TERMINATED;
        workThread.interrupt();
        log.info("ConsumerWorker name {} stop topic {} status {}", name,topic, status.name());
    }

    public Status getStatus() {
        return status;
    }

    public enum Status {
        INIT,
        RUNNING,
        TERMINATED,
    }
}
