package com.homo.core.root.config;

import com.homo.core.facade.document.EntityStorageDriver;
import com.homo.core.root.storage.ByteStorage;
import com.homo.core.root.storage.EntityStorage;
import com.homo.core.root.storage.ObjStorage;
import com.homo.core.root.storage.RectorEntityStorage;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

;

@Configuration
@Log4j2
public class CoreAutoConfiguration {

    @Bean("byteStorage")
    @DependsOn("storageDriver")
    public ByteStorage byteStorage(){
        return new ByteStorage();
    }

    @Bean("objStorage")
    @DependsOn("byteStorage")
    public ObjStorage objStorage(){
        return new ObjStorage();
    }

    @Bean("entityStorage")
    @ConditionalOnBean(EntityStorageDriver.class)
    public EntityStorage entityStorage(){
        return new EntityStorage();
    }

    @Bean("RectorEntityStorage")
    public RectorEntityStorage rectorEntityStorage(){
        return new RectorEntityStorage();
    }
}
