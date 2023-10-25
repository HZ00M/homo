package com.homo.core.utils.config;

import com.homo.core.utils.serial.FastjsonSerializationProcessor;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import com.homo.core.utils.serial.JacksonSerializationProcessor;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
public class UtilsAutoConfiguration {
    @Bean("getBeanUtil")
    public GetBeanUtil getBeanUtil(){
        log.info("register bean getBeanUtil");
        return new GetBeanUtil();
    }

    @Bean("homoSerializationProcessor")
    @ConditionalOnMissingBean
    public HomoSerializationProcessor fastjsonSerializationProcessor(){
        log.info("register bean homoSerializationProcessor implement FastjsonSerializationProcessor");
        return new FastjsonSerializationProcessor();
    }
    @Bean("homoSerializationProcessor")
    @ConditionalOnProperty(
            prefix = "homo.serial",
            name = {"type"},
            havingValue = "fastjson")
    public HomoSerializationProcessor jacksonSerializationProcessor(){
        log.info("register bean homoSerializationProcessor implement JacksonSerializationProcessor");
        return new JacksonSerializationProcessor();
    }
}
