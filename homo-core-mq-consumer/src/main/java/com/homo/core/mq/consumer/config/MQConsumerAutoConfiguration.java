package com.homo.core.mq.consumer.config;

import com.homo.core.configurable.mq.MQCoreProperties;
import com.homo.core.facade.mq.consumer.MQConsumerFactory;
import com.homo.core.facade.mq.consumer.SinkHandler;
import com.homo.core.mq.consumer.MQConsumerFactoryImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.Map;

@Configuration
@Slf4j
@Import(MQCoreProperties.class)
public class MQConsumerAutoConfiguration {
    @Autowired
    MQCoreProperties mqCoreProperties;

    @Autowired
    Map<String, SinkHandler> sinkHandlerMap;

    @Bean("mqConsumerFactory")
    public MQConsumerFactory mqConsumerFactory(){
        log.info("register bean mqConsumerFactory");
        MQConsumerFactory mqConsumerFactory = new MQConsumerFactoryImpl(mqCoreProperties);
        return mqConsumerFactory;
    }
}
