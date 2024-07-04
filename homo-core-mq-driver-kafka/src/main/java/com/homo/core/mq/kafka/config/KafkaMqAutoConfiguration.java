package com.homo.core.mq.kafka.config;

import com.homo.core.configurable.mq.MQKafkaProperties;
import com.homo.core.facade.mq.consumer.MQConsumerFactory;
import com.homo.core.facade.mq.producer.MQProducerFactory;
import com.homo.core.mq.base.MQDriverFactoryProvider;
import com.homo.core.mq.kafka.consumer.MQKafkaConsumerDriverFactory;
import com.homo.core.mq.kafka.producer.MQKafkaProducerDriverFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Slf4j
@Import(MQKafkaProperties.class)
public class KafkaMqAutoConfiguration {

    @Autowired
    MQKafkaProperties kafkaProperties;

    @Bean
    @ConditionalOnBean(value = MQProducerFactory.class)
    public MQKafkaProducerDriverFactory mqKafkaProducerDriverFactory(){
        log.info("register bean mqProducerDriverFactory");
        MQKafkaProducerDriverFactory factory = new MQKafkaProducerDriverFactory(kafkaProperties);
        MQDriverFactoryProvider.producerFactoryMap.put(factory.getType(),factory);
        return factory;
    }

    @Bean
    @ConditionalOnBean(value = MQConsumerFactory.class)
    public MQKafkaConsumerDriverFactory mqKafkaConsumerDriverFactory(){
        log.info("register bean mqConsumerDriverFactory");
        MQKafkaConsumerDriverFactory factory = new MQKafkaConsumerDriverFactory(kafkaProperties);
        MQDriverFactoryProvider.consumerFactoryMap.put(factory.getType(),factory);
        return factory;
    }

}
