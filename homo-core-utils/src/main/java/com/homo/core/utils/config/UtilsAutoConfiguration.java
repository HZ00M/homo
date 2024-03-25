package com.homo.core.utils.config;

import com.homo.core.configurable.module.ModuleProperties;
import com.homo.core.utils.apollo.ApolloConfigDriver;
import com.homo.core.utils.apollo.ConfigDriver;
import com.homo.core.utils.apollo.PropertyProcessor;
import com.homo.core.utils.http.HttpCallerFactory;
import com.homo.core.utils.log.HomoLogHandler;
import com.homo.core.utils.module.ModuleMgr;
import com.homo.core.utils.module.ModuleMgrImpl;
import com.homo.core.utils.module.RootModule;
import com.homo.core.utils.module.RootModuleImpl;
import com.homo.core.utils.serial.FastjsonSerializationProcessor;
import com.homo.core.utils.serial.HomoSerializationProcessor;
import com.homo.core.utils.serial.JacksonSerializationProcessor;
import com.homo.core.utils.spring.GetBeanUtil;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

import java.util.Set;

@Slf4j
@Import({ModuleProperties.class, ZipKinProperties.class})
@Configuration
public class UtilsAutoConfiguration {
    @Bean("homoLogHandler")
    public HomoLogHandler homoLogHandler(){
        log.info("register bean homoLogHandler");
        HomoLogHandler homoLogHandler = new HomoLogHandler();
        return homoLogHandler;
    }

    @Bean("configDriver")
    public ConfigDriver configDriver(){
        log.info("register bean configDriver ApolloConfigDriver");
        ApolloConfigDriver configDriver = new ApolloConfigDriver();
        Set<String> namespaces = PropertyProcessor.getInstance().getNamespaces();
        configDriver.init(namespaces,null);
        return  configDriver;
    }

    @Bean("zipkinUtil")
    @DependsOn({"configDriver"})
    public ZipkinUtil zipkinUtil(){
        log.info("register bean zipkinUtil");
        ZipkinUtil zipkinUtil = new ZipkinUtil();
        return zipkinUtil;
    }

    @Bean("httpCallerFactory")
    @ConditionalOnMissingBean
    public HttpCallerFactory httpCallerFactory(){
        log.info("register bean httpCallerFactory");
        return new HttpCallerFactory();
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
