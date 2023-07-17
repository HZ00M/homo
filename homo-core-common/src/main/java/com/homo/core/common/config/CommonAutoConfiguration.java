package com.homo.core.common.config;

import com.homo.core.common.apollo.ApolloConfigDriver;
import com.homo.core.common.apollo.ConfigDriver;
import com.homo.core.common.apollo.PropertyProcessor;
import com.homo.core.common.http.HttpCallerFactory;
import com.homo.core.common.module.ModuleMgrImpl;
import com.homo.core.common.module.RootModuleImpl;
import com.homo.core.configurable.module.ModuleProperties;
import com.homo.core.facade.module.ModuleMgr;
import com.homo.core.facade.module.RootModule;
import com.homo.core.utils.config.ZipKinProperties;
import com.homo.core.utils.trace.ZipkinUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@Configuration
@Import({ModuleProperties.class, ZipKinProperties.class})
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

    @Bean("zipkinUtil")
    @DependsOn("configDriver")
    public ZipkinUtil zipkinUtil(ConfigDriver configDriver,ZipKinProperties zipKinProperties){
        log.info("register bean zipkinUtil");
        configDriver.registerNamespace(zipKinProperties.zipikinNamespace);
        ZipkinUtil zipkinUtil = new ZipkinUtil();
        zipkinUtil.init(zipKinProperties);
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
}
