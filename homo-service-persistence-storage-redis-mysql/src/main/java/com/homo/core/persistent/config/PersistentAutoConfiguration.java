package com.homo.core.persistent.config;

import com.homo.core.facade.storege.StorageDriver;
import com.homo.core.persistent.storage.MysqlRedisStorageDriverImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


@Log4j2
@Configuration
public class PersistentAutoConfiguration {

    @Bean("storageDriver")
    @DependsOn("dirtyDriver")
    public StorageDriver storageDriver(){
        log.info("register bean storageDriver");
        return new MysqlRedisStorageDriverImpl();
    }
}
