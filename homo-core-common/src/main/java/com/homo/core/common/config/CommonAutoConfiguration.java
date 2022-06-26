package com.homo.core.common.config;

import com.homo.core.common.apollo.ApolloConfigDriver;
import com.homo.core.common.apollo.ConfigDriver;
import com.homo.core.common.apollo.PropertyProcessor;
import com.homo.core.common.module.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@Slf4j
public class CommonAutoConfiguration {

    @Bean("configDriver")
    public ConfigDriver configDriver(){
        log.info("register bean configDriver");
        ConfigDriver configDriver = new ApolloConfigDriver();
        String[] namespaces = PropertyProcessor.getInstance().getNamespaces();
        configDriver.init(namespaces,null);
        return  configDriver;
    }

    @Bean("rootModule")
    @DependsOn("configDriver")
    public RootModule rootModule(){
        log.info("register bean rootModule");
        RootModule rootModule = new RootModuleImpl();
        return rootModule;
    }

    @Bean("moduleMgr")
    public ModuleMgr moduleMgr(){
        log.info("register bean moduleMgr");
        ModuleMgr moduleMgr = new ModuleMgrImpl();
        return moduleMgr;
    }

}
