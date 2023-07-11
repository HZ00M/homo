package com.homo.core.utils.config;

import com.homo.core.common.apollo.ConfigDriver;
import com.homo.core.common.config.CommonAutoConfiguration;
import com.homo.core.configurable.zipkin.ZipKinProperties;
import com.homo.core.utils.http.HttpCallerFactory;
import com.homo.core.utils.module.ModuleMgr;
import com.homo.core.utils.module.ModuleMgrImpl;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.module.RootModuleImpl;
import com.homo.core.utils.serial.FastjsonSerializationProcessor;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import com.homo.core.utils.serial.JacksonSerializationProcessor;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@Log4j2
@Configuration
@Import({ZipKinProperties.class})
@AutoConfigureAfter(CommonAutoConfiguration.class)
public class UtilsAutoConfiguration {
    @Autowired
    ZipKinProperties zipKinProperties;

    @Bean("getBeanUtil")
    public GetBeanUtil getBeanUtil(){
        log.info("register bean getBeanUtil");
        return new GetBeanUtil();
    }
    @Bean("rootModule")
    @DependsOn("configDriver")
    public RootModule rootModule(){
        log.info("register bean rootModule");
        RootModule rootModule = new RootModuleImpl();
        return rootModule;
    }

    @Bean("moduleMgr")
    @DependsOn("rootModule")
    public ModuleMgr moduleMgr(){
        log.info("register bean moduleMgr");
        ModuleMgr moduleMgr = new ModuleMgrImpl();
        return moduleMgr;
    }

    @Bean("zipkinUtil")
    @DependsOn("configDriver")
    public ZipkinUtil zipkinUtil(ConfigDriver configDriver){
        log.info("register bean zipkinUtil");
        configDriver.registerNamespace(zipKinProperties.zipikinNamespace);
        return new ZipkinUtil();
    }

    @Bean("homoSerializationProcessor")
    @ConditionalOnMissingBean
    public HomoSerializationProcessor fastjsonSerializationProcessor(){
        log.info("register bean homoSerializationProcessor implement FastjsonSerializationProcessor");
        return new FastjsonSerializationProcessor();
    }

    @Bean("httpCallerFactory")
    @ConditionalOnMissingBean
    public HttpCallerFactory httpCallerFactory(){
        log.info("register bean httpCallerFactory");
        return new HttpCallerFactory();
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
