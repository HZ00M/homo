package com.homo.core.redis.config;

import com.homo.core.common.apollo.ConfigDriver;
import com.homo.core.redis.enums.ERedisType;
import com.homo.core.redis.facade.HomoRedisPool;
import com.homo.core.redis.factory.HomoJedisPoolCreater;
import com.homo.core.redis.factory.HomoJodisPoolCreater;
import com.homo.core.redis.factory.HomoLettucePoolCreater;
import com.homo.core.redis.factory.RedisInfoHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@Slf4j
public class RedisPoolAutoConfigure  {
    @Value("${redis.public.namespace:homo_redis_config}")
    private String publicNamespace;
    @Value("${redis.private.namespace:redis_connect_info}")
    private String privateNamespace;
    @Value("${redis.type:LETTUCE_POOL}")
    private String redisType;

    @DependsOn("configDriver")
    @Bean("redisInfoHolder")
    public RedisInfoHolder redisInfoHolder(ConfigDriver configDriver){
        log.info("register bean redisInfoHolder");
        configDriver.registerNamespace(publicNamespace);
        configDriver.registerNamespace(privateNamespace);
        RedisInfoHolder holder = new RedisInfoHolder(publicNamespace,privateNamespace,configDriver);
        return holder;
    }

    @DependsOn({"redisInfoHolder","getBeanUtil"})
    @Bean("homoRedisPool")
    public HomoRedisPool homoRedisPool() throws InterruptedException{
        log.info("register bean homoRedisPool");
        if(redisType.equals("")){
            log.error("cannot find redis type {}",redisType);
            return null;
        }
        if (redisType.equals (ERedisType.JEDIS_POOL.name())){
            return HomoJedisPoolCreater.createPool();
        } else if (redisType.equals (ERedisType.JODIS_POOL.name())){
            return HomoJodisPoolCreater.createPool();
        } else if (redisType.equals(ERedisType.JEDIS_CLUSTER.name())){
            return HomoJedisPoolCreater.createPool();
        } else if (redisType.equals(ERedisType.LETTUCE_POOL.name())) {
            return HomoLettucePoolCreater.createPool();
        }
        return null;
    }

}
