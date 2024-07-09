package com.homo.core.persistent.config;

import com.homo.core.facade.storege.StorageDriver;
import com.homo.core.persistent.storage.MysqlRedisStorageDriverImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;


@Slf4j
@AutoConfiguration
public class PersistentAutoConfiguration {

    @Bean("storageDriver")
    @DependsOn("dirtyDriver")
    public StorageDriver storageDriver(){
        log.info("register bean storageDriver");
        return new MysqlRedisStorageDriverImpl();
    }
}
