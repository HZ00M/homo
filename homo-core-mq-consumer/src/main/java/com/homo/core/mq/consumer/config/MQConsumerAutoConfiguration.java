package com.homo.core.mq.consumer.config;

import com.homo.core.facade.mq.consumer.MQConsumerFactory;
import com.homo.core.mq.consumer.MQConsumerFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@Slf4j
public class MQConsumerAutoConfiguration {

    @Bean("mqConsumerFactory")
    public MQConsumerFactory mqConsumerFactory(){
        log.info("register bean mqConsumerFactory");
        MQConsumerFactory mqConsumerFactory = new MQConsumerFactoryImpl();
        return mqConsumerFactory;
    }
}
