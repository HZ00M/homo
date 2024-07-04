package com.homo.core.mq.kafka.producer;

import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.producer.MQProducerDriver;
import com.homo.core.facade.mq.producer.ProducerCallback;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.Callback;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.jetbrains.annotations.NotNull;

import java.util.Properties;
import java.util.concurrent.ThreadPoolExecutor;

@Slf4j
public class MQKafkaProducerDriver implements MQProducerDriver {
    Properties properties;
    KafkaProducerTemplate<String,byte[]> template;
    public MQKafkaProducerDriver(Properties properties, ThreadPoolExecutor executor){

        this.properties = properties;
        this.template = new KafkaProducerTemplate<>(properties, executor);
    }

    @Override
    public @NotNull MQType getType() {
        return MQType.KAFKA;
    }

    @Override
    public void sendMessage(@NotNull String topic, byte @NotNull [] message) {
        template.sendMessage(topic,message);
    }

    @Override
    public void sendMessage(@NotNull String topic, byte @NotNull [] message, @NotNull ProducerCallback callback) {
        try {
            template.sendMessage(topic, null, message, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (e!= null){
                        callback.onCompletion(false,e);
                    }else {
                        callback.onCompletion(true,null);
                    }
                }
            });
        }catch (Exception e){
            callback.onCompletion(false,e);
        }
    }

    @Override
    public void sendMessage(@NotNull String topic, String key, byte @NotNull [] message, @NotNull ProducerCallback callback) {
        try {
            template.sendMessage(topic, key, message, new Callback() {
                @Override
                public void onCompletion(RecordMetadata recordMetadata, Exception e) {
                    if (e!= null){
                        callback.onCompletion(false,e);
                    }else {
                        callback.onCompletion(true,null);
                    }
                }
            });
        }catch (Exception e){
            callback.onCompletion(false,e);
        }
    }

    @Override
    public void close() throws Exception {
        template.close();
    }
}
