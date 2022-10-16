package com.homo.service.redis.lock.config;

import com.homo.service.redis.lock.driver.RedisLockDriver;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;


@Configuration
@Log4j2
public class RedisLockAutoConfigure {


    @Bean("homoLockDriver")
    @DependsOn({"homoRedisPool"})
    public RedisLockDriver homoLockDriver(){
        log.info("register bean homoLockDriver");
        RedisLockDriver redisLockDriver = new RedisLockDriver();
        return redisLockDriver;
    }
}
