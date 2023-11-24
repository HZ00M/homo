package com.homo.core.redis.factory;

import com.homo.core.redis.facade.HomoRedisPool;
import com.homo.core.redis.impl.HomoJodisPool;
import com.homo.core.utils.spring.GetBeanUtil;
import io.codis.jodis.RoundRobinJedisPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

;

@Slf4j
public class HomoJodisPoolCreater {
    public static HomoRedisPool createPool(){
        try {
            RedisInfoHolder redisInfoHolder = GetBeanUtil.getBean(RedisInfoHolder.class); 
            if(StringUtils.isEmpty(redisInfoHolder.getProxyDir()) || StringUtils.isEmpty(redisInfoHolder.getUrl())){
                log.warn("jodisProxyDir or jodisConnectString is null , could not init the tpfJodisPool");
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
            log.info("HomoJodisPool create config {}",config);
            RoundRobinJedisPool robinJedisPool;
            if(StringUtils.isEmpty(redisInfoHolder.getAuth())){
                robinJedisPool = RoundRobinJedisPool.create()
                        .curatorClient(redisInfoHolder.getUrl(), 30000)
                        .zkProxyDir(redisInfoHolder.getProxyDir())
                        .poolConfig(config)
                        .database(redisInfoHolder.getDataBase())
                        .connectionTimeoutMs(redisInfoHolder.getSoTimeOut())
                        .timeoutMs(redisInfoHolder.getTimeOutMs())
                        .build();
            }else{
                robinJedisPool = RoundRobinJedisPool.create()
                        .curatorClient(redisInfoHolder.getUrl(), 30000)
                        .zkProxyDir(redisInfoHolder.getProxyDir())
                        .password(redisInfoHolder.getAuth())
                        .poolConfig(config)
                        .database(redisInfoHolder.getDataBase())
                        .connectionTimeoutMs(redisInfoHolder.getSoTimeOut())
                        .timeoutMs(redisInfoHolder.getTimeOutMs())
                        .build();
            }
            HomoJodisPool homoJodisPool = new HomoJodisPool(robinJedisPool);
            return homoJodisPool;
        }catch (Exception e){
            log.error("HomoJodisPool create error_{]", e);
            e.printStackTrace();
            return null;
        }
    }
}
