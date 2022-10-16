package com.homo.service.state.driver.redis.config;

import com.homo.core.facade.service.StatefulDriver;
import com.homo.service.state.driver.redis.StatefulDriverRedisImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@Log4j2
public class RedisStatefulAutoConfiguration {

    @DependsOn("homoRedisPool")
    @Bean("statefulDriver")
    public StatefulDriver statefulDriver(){
        log.info("register bean statefulDriver");
        return new StatefulDriverRedisImpl();
    }
}
