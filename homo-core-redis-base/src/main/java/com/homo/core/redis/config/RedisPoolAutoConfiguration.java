package com.homo.core.redis.config;

import com.homo.core.configurable.redis.RedisNamespaceProperties;
import com.homo.core.configurable.redis.RedisProperties;
import com.homo.core.redis.enums.ERedisType;
import com.homo.core.redis.facade.HomoRedisPool;
import com.homo.core.redis.factory.HomoJedisPoolCreater;
import com.homo.core.redis.factory.HomoJodisPoolCreater;
import com.homo.core.redis.factory.HomoLettucePoolCreater;
import com.homo.core.redis.factory.RedisInfoHolder;
import com.homo.core.utils.apollo.ConfigDriver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Slf4j
@Import({RedisNamespaceProperties.class, RedisProperties.class})
public class RedisPoolAutoConfiguration {
    @Autowired
    private RedisNamespaceProperties connectProperties;
    @Autowired
    private RedisProperties properties;

    @DependsOn("configDriver")
    @Bean("redisInfoHolder")
    public RedisInfoHolder redisInfoHolder(ConfigDriver configDriver){
        log.info("register bean redisInfoHolder");
        configDriver.registerNamespace(connectProperties.getPublicNamespace());
        configDriver.registerNamespace(connectProperties.getPrivateNamespace());
        RedisInfoHolder holder = new RedisInfoHolder(connectProperties.getPublicNamespace(),connectProperties.getPrivateNamespace(),configDriver,properties);
        return holder;
    }

    @DependsOn({"redisInfoHolder","getBeanUtil"})
    @Bean("homoRedisPool")
    public HomoRedisPool homoRedisPool() throws InterruptedException{
        log.info("register bean homoRedisPool");
        String redisType = connectProperties.getRedisType();
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
