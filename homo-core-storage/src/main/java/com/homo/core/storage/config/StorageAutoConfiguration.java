package com.homo.core.storage.config;

import com.homo.core.storage.ByteStorage;
import com.homo.core.storage.ObjStorage;
import com.homo.core.storage.DocumentStorage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;


@AutoConfiguration
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

    @Bean("documentStorage")
    public DocumentStorage documentStorage(){
        return new DocumentStorage();
    }
}
