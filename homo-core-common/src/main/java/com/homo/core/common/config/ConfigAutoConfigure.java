package com.homo.core.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class ConfigAutoConfigure  {

    @Bean("configDriver")
    public ConfigDriver configDriver(){
        ApolloConfigDriver apolloConfigDriver = new ApolloConfigDriver();
        String[] namespaces = PropertyProcessor.getInstance().getNamespaces();
        apolloConfigDriver.init(namespaces,null);
        return  apolloConfigDriver;
    }

}
