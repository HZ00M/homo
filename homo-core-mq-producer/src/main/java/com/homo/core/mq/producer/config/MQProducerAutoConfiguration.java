package com.homo.core.mq.producer.config;

import com.homo.core.facade.mq.producer.MQProducerFactory;
import com.homo.core.mq.producer.MQProducerFactoryImpl;
import com.homo.core.utils.config.UtilsAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = {UtilsAutoConfiguration.class})
@Slf4j
//@AutoConfigureOrder(3)
//@AutoConfigureAfter(value = {UtilsAutoConfiguration.class})
public class MQProducerAutoConfiguration {

    @Bean("mqProducerFactory")
    public MQProducerFactory mqProducerFactory(){
        log.info("register bean mqProducerFactory");
        MQProducerFactory mqProducerFactory = new MQProducerFactoryImpl();
        return mqProducerFactory;
    }

}
