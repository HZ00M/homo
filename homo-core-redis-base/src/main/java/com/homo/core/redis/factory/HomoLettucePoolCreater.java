package com.homo.core.redis.factory;

import com.homo.concurrent.thread.ThreadPoolFactory;
import com.homo.core.redis.facade.HomoRedisPool;
import com.homo.core.redis.impl.HomoLettucePool;
import com.homo.core.utils.spring.GetBeanUtil;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.resource.DefaultClientResources;
import io.lettuce.core.resource.DefaultEventLoopGroupProvider;
import io.lettuce.core.resource.Delay;
import io.netty.channel.nio.NioEventLoopGroup;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

;

@Log4j2
public class HomoLettucePoolCreater {
    public static HomoRedisPool createPool()throws InterruptedException{
        try {
            RedisInfoHolder redisInfoHolder = GetBeanUtil.getBean(RedisInfoHolder.class);
            if(StringUtils.isEmpty(redisInfoHolder.getUrl()) || StringUtils.isEmpty(redisInfoHolder.getPort())){
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
            log.info("HomoLettucePool create config_{}",config);

            RedisURI.Builder redisUriBuilder = RedisURI.builder()
                    .withHost(redisInfoHolder.getUrl())
                    .withPort(redisInfoHolder.getPort())
                    .withDatabase(redisInfoHolder.getDataBase());


            if(!StringUtils.isEmpty(redisInfoHolder.getAuth())){
                redisUriBuilder.withPassword(redisInfoHolder.getAuth());
            }
            DefaultClientResources resources = DefaultClientResources.builder()
                    .eventExecutorGroup(new NioEventLoopGroup(2, ThreadPoolFactory.newThreadPool("HOMO-REDIS", 2, 0)))
                    .eventLoopGroupProvider(new DefaultEventLoopGroupProvider(2,
                            ThreadPoolFactory::newThreadFactory))
                    .reconnectDelay(Delay.constant(Duration.ofSeconds(20)))
                    .build();
            HomoLettucePool homoJedisPool = new HomoLettucePool(RedisClient.create(resources,redisUriBuilder.build()));
            return homoJedisPool;
        }catch (Exception e){
            log.error("HomoLettucePool create error_{]", e);
            e.printStackTrace();
            Thread.sleep(1000);
            return createPool();
        }
    }
}
