package com.homo.core.redis.factory;

import com.homo.core.redis.enums.ERedisType;
import com.homo.core.redis.facade.HomoRedisPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedisPoolMgr {
    @Value("homo.redis.namespace")
    private String redisNs;
    @Autowired
    private RedisInfoHolder holder;

    @Bean
    public HomoRedisPool getHomoRedisPool() throws InterruptedException{
        holder.load(redisNs);
        String redisType = holder.getRedisType();
        if(redisType.equals("")){
            log.error("cannot find redis type {}",holder.getRedisType());
            return null;
        }
        if (redisType.equals (ERedisType.JEDIS_POOL)){
            return HomoJedisPoolCreater.createPool();
        } else if (redisType.equals ("JodisPool")){
            return HomoJodisPoolCreater.createPool();
        } else if (redisType.equals("JedisCluster")){
            return HomoJedisPoolCreater.createPool();
        } else if (redisType.equals("LettucePool")) {
            return HomoLettucePoolCreater.createPool();
        }

        return null;
    }
}
