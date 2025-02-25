package com.homo.service.dirty.config;

import com.homo.core.configurable.dirty.DirtyProperties;
import com.homo.service.dirty.PersistentProcess;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;



@Slf4j
@Import({DirtyProperties.class})
@AutoConfiguration
public class DirtyProccessAutoConfiguration {

    @Autowired
    DirtyProperties dirtyProperties;

    @Bean("persistentProcess")
    @DependsOn({"dbDataHolder","dirtyDriver"})
    public PersistentProcess persistentProcess(){
        log.info("register bean persistentProcess");
        PersistentProcess persistentProcess = new PersistentProcess();
        return persistentProcess;
    }
}
