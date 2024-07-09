package com.homo.core.mq.kafka.config;

import com.homo.core.configurable.mq.MQKafkaProperties;
import com.homo.core.mq.base.MQDriverFactoryProvider;
import com.homo.core.mq.kafka.consumer.MQKafkaConsumerDriverFactory;
import com.homo.core.mq.kafka.producer.MQKafkaProducerDriverFactory;
import com.homo.core.utils.config.UtilsAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration(after = {UtilsAutoConfiguration.class})
@Slf4j
@Import(MQKafkaProperties.class)
//@AutoConfigureOrder(2)
//@AutoConfigureAfter(value = {UtilsAutoConfiguration.class})
public class KafkaMqAutoConfiguration {

    @Autowired
    MQKafkaProperties kafkaProperties;

    @Bean
    public MQKafkaProducerDriverFactory mqKafkaProducerDriverFactory(){
        log.info("register bean MQKafkaProducerDriverFactory");
        MQKafkaProducerDriverFactory factory = new MQKafkaProducerDriverFactory(kafkaProperties);
        MQDriverFactoryProvider.producerFactoryMap.put(factory.getType(),factory);
        return factory;
    }

    @Bean
    public MQKafkaConsumerDriverFactory mqKafkaConsumerDriverFactory(){
        log.info("register bean mqConsumerDriverFactory");
        MQKafkaConsumerDriverFactory factory = new MQKafkaConsumerDriverFactory(kafkaProperties);
        MQDriverFactoryProvider.consumerFactoryMap.put(factory.getType(),factory);
        return factory;
    }

}
