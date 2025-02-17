package com.homo.service.cache.config;

import com.homo.core.facade.cache.CacheDriver;
import com.homo.service.cache.driver.RedisCacheDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


@Slf4j
@Configuration
public class RedisCacheDriverAutoConfigure {
    @Bean("cacheDriver")
    @DependsOn("homoRedisPool")
    public CacheDriver cacheDriver() {
        log.info("register bean cacheDriver");
        return new RedisCacheDriver();
    }


}
