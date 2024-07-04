package com.homo.core.mq.producer.config;

import com.homo.core.configurable.mq.MQCoreProperties;
import com.homo.core.facade.mq.producer.MQProducerFactory;
import com.homo.core.mq.producer.MQProducerFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Slf4j
@Import(MQCoreProperties.class)
public class MQProducerAutoConfiguration {
    @Autowired
    MQCoreProperties mqCoreProperties;

    @Bean("mqProducerFactory")
    public MQProducerFactory mqProducerFactory(){
        log.info("register bean mqProducerFactory");
        MQProducerFactory mqProducerFactory = new MQProducerFactoryImpl();
        return mqProducerFactory;
    }
}
