package com.homo.service.state.driver.redis.config;

import com.homo.core.facade.service.StatefulDriver;
import com.homo.service.state.driver.redis.StatefulDriverRedisImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

@AutoConfiguration
@Slf4j
public class RedisStatefulAutoConfiguration {

    @DependsOn("homoRedisPool")
    @Bean("statefulDriver")
    public StatefulDriver statefulDriver(){
        log.info("register bean statefulDriver");
        return new StatefulDriverRedisImpl();
    }
}
