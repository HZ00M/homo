package com.homo.core.common.config;

import com.homo.core.common.apollo.ApolloConfigDriver;
import com.homo.core.common.apollo.ConfigDriver;
import com.homo.core.common.apollo.PropertyProcessor;
import com.homo.core.common.module.ModuleMgr;
import com.homo.core.common.module.ModuleMgrImpl;
import com.homo.core.common.module.RootModule;
import com.homo.core.common.module.RootModuleImpl;
import com.homo.core.configurable.module.ModuleProperties;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@Configuration
@Import(ModuleProperties.class)
@Log4j2
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
    @DependsOn("rootModule")
    public ModuleMgr moduleMgr(){
        log.info("register bean moduleMgr");
        ModuleMgr moduleMgr = new ModuleMgrImpl();
        return moduleMgr;
    }

}
