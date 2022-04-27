package com.homo.core.redis.config;

import com.homo.core.common.config.ConfigDriver;
import com.homo.core.configurable.redis.JedisClusterProperties;
import com.homo.core.configurable.redis.JedisProperties;
import com.homo.core.configurable.redis.JodisProperties;
import com.homo.core.redis.enums.ERedisType;
import com.homo.core.redis.facade.HomoRedisPool;
import com.homo.core.redis.factory.HomoJedisPoolCreater;
import com.homo.core.redis.factory.HomoJodisPoolCreater;
import com.homo.core.redis.factory.HomoLettucePoolCreater;
import com.homo.core.redis.factory.RedisInfoHolder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@EnableConfigurationProperties({JedisProperties.class,JedisClusterProperties.class, JodisProperties.class})
@Slf4j
public class RedisPoolAutoConfigure  {
    @Value("${redis.namespace}")
    private String redisNs;
    @Value("${redis.type}")
    private String redisType;

    @DependsOn("configDriver")
    @Bean("redisInfoHolder")
    public RedisInfoHolder redisInfoHolder(ConfigDriver configDriver){
        RedisInfoHolder holder = new RedisInfoHolder(configDriver,redisNs);
        return holder;
    }

    @DependsOn({"redisInfoHolder","getBeanUtil"})
    @Bean("homoRedisPool")
    public HomoRedisPool homoRedisPool() throws InterruptedException{
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
