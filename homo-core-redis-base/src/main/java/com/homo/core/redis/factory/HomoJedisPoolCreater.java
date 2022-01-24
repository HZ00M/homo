package com.homo.core.redis.factory;

import com.homo.core.configurable.redis.JedisProperties;
import com.homo.core.redis.facade.HomoRedisPool;
import com.homo.core.redis.impl.HomoJedisPool;
import com.homo.core.utils.spring.GetBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

@Slf4j
public class HomoJedisPoolCreater {
    public static HomoRedisPool createPool(){
        try {
            RedisInfoHolder redisInfoHolder = GetBeanUtil.getBean(RedisInfoHolder.class);
            JedisProperties jedisProperties = GetBeanUtil.getBean(JedisProperties.class);
            if(StringUtils.isEmpty(jedisProperties.getUrl()) || StringUtils.isEmpty(jedisProperties.getPort())){
                log.warn("jedisUrl or jedisPort is null , could not init the tpfJedisPool");
                return null;
            }
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(redisInfoHolder.getMaxTotal());
            config.setMaxIdle(redisInfoHolder.getMaxIdle());
            config.setMaxIdle(redisInfoHolder.getMinIdel());
            config.setMaxWaitMillis(redisInfoHolder.getMaxWaitMillis());
            /*
            testOnBorrow能够确保我们每次都能获取到可用的连接，
            但是如果设置为true，则每次获取连接时候都要到数据库验证连接有效性，
            这在高并发的时候会造成性能下降，可以将testOnBorrow设置成false，
            testWhileIdle设置成true这样能获得比较好的性能。
             */
            config.setTestOnBorrow(redisInfoHolder.isTestOnBorrow());
            log.info("HomoJedisPool create config_{}",config);
            JedisPool jedisPool;
            if(StringUtils.isEmpty(jedisProperties.getAuth())){
                jedisPool = new JedisPool(config, jedisProperties.getUrl(), jedisProperties.getPort(),redisInfoHolder.getTimeOutMs(),(String) null, redisInfoHolder.getDataBase(),(String) null);
            }else{
                jedisPool = new JedisPool(config, jedisProperties.getUrl(), jedisProperties.getPort(),redisInfoHolder.getTimeOutMs(), jedisProperties.getAuth(), redisInfoHolder.getDataBase(),(String) null);
            }
            HomoJedisPool homoJedisPool = new HomoJedisPool(jedisPool);
            return homoJedisPool;
        }catch (Exception e){
            log.error("HomoJedisPool create error_{]", e);
            e.printStackTrace();
            return null;
        }
    }
}
