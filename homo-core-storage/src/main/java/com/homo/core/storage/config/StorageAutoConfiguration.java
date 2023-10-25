package com.homo.core.storage.config;

import com.homo.core.facade.document.EntityStorageDriver;
import com.homo.core.storage.ByteStorage;
import com.homo.core.storage.EntityStorage;
import com.homo.core.storage.ObjStorage;
import com.homo.core.storage.RectorEntityStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

;

@Configuration
@Slf4j
public class StorageAutoConfiguration {

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
