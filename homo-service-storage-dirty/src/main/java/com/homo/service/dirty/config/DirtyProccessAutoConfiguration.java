package com.homo.service.dirty.config;

import com.homo.core.configurable.dirty.DirtyProperties;
import com.homo.service.dirty.PersistentProcess;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

;


@Log4j2
@Import({DirtyProperties.class})
@Configuration
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
