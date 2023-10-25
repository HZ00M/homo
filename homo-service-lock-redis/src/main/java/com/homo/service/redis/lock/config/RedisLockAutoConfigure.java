package com.homo.service.redis.lock.config;

import com.homo.service.redis.lock.driver.RedisLockDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


@Configuration
@Slf4j
public class RedisLockAutoConfigure {


    @Bean("homoLockDriver")
    @DependsOn({"homoRedisPool"})
    public RedisLockDriver homoLockDriver(){
        log.info("register bean homoLockDriver");
        RedisLockDriver redisLockDriver = new RedisLockDriver();
        return redisLockDriver;
    }
}
