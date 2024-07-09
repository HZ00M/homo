package com.homo.core.mq.kafka.producer;

import com.homo.core.facade.mq.MQType;
import com.homo.core.facade.mq.producer.MQProducerDriver;
import com.homo.core.facade.mq.producer.ProducerCallback;
import lombok.extern.slf4j.Slf4j;
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
    public void sendMessage(@NotNull String topic, String key, byte @NotNull [] message,  ProducerCallback callback) {
        try {

            template.sendMessage(topic, key, message, (recordMetadata, e) -> {
                if (callback == null){
                    log.debug("sendMessage topic {} topic {} recordMetadata {} e",topic,recordMetadata,e);
                    return;
                }
                if (e!= null){
                    callback.onCompletion(false,e);
                }else {
                    callback.onCompletion(true,null);
                }
            });
        }catch (Exception e){
            if (callback == null){
                log.debug("sendMessage topic {} topic {}  e",topic,e);
                return;
            }
            callback.onCompletion(false,e);
        }
    }

    @Override
    public void close() throws Exception {
        template.close();
    }
}
