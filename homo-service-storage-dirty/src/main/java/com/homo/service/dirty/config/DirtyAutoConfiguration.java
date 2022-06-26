package com.homo.service.dirty.config;

import com.homo.core.configurable.dirty.DirtyProperties;
import com.homo.core.facade.lock.LockDriver;
import com.homo.core.facade.storege.dirty.DirtyDriver;
import com.homo.core.facade.storege.dirty.DirtyHelper;
import com.homo.service.dirty.PersistentProcess;
import com.homo.service.dirty.RedisDirtyDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;


@Slf4j
@Import({DirtyProperties.class})
@Configuration
public class DirtyAutoConfiguration {

    @Autowired
    DirtyProperties dirtyProperties;

    @Bean("dirtyDriver")
    @DependsOn({"homoLockDriver"})
    public DirtyDriver dirtyDriver(){
        log.info("register bean dirtyDriver");
        DirtyHelper.init(dirtyProperties);
        return new RedisDirtyDriver();
    }

    @Bean("persistentProcess")
    @DependsOn({"dbDataHolder","dirtyDriver"})
    public PersistentProcess persistentProcess(){
        log.info("register bean persistentProcess");
        PersistentProcess persistentProcess = new PersistentProcess();
        return persistentProcess;
    }
}
