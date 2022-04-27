package com.homo.service.lock.configure;

import com.homo.core.redis.facade.HomoAsyncRedisPool;
import com.homo.core.redis.lua.LuaScriptHelper;
import com.homo.service.lock.driver.RedisLockDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@Slf4j
public class RedisLockAutoConfigure {

    @Bean
    LuaScriptHelper luaScriptHelper(){
        LuaScriptHelper luaScriptHelper = new LuaScriptHelper();
        luaScriptHelper.init();
        return luaScriptHelper;
    }

    @Bean
    @DependsOn("homoRedisPool")
    public RedisLockDriver lockDriver(HomoAsyncRedisPool homoAsyncRedisPool){
        RedisLockDriver redisLockDriver = new RedisLockDriver();
        redisLockDriver.init(homoAsyncRedisPool);
        return redisLockDriver;
    }
}
