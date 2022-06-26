package com.homo.core.utils.config;

import com.homo.core.common.apollo.ConfigDriver;
import com.homo.core.configurable.zipkin.ZipKinProperties;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@Slf4j
@Configuration
@Import({ZipKinProperties.class})
public class UtilsAutoConfiguration {
    @Autowired
    ZipKinProperties zipKinProperties;

    @Bean("getBeanUtil")
    public GetBeanUtil getBeanUtil(){
        return new GetBeanUtil();
    }


    @Bean("zipkinUtil")
    @DependsOn("configDriver")
    public ZipkinUtil zipkinUtil(ConfigDriver configDriver){
        log.info("register bean zipkinUtil");
        configDriver.registerNamespace(zipKinProperties.zipikinNamespace);
        return new ZipkinUtil();
    }
}
