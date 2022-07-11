package com.homo.core.redis.factory;

import com.homo.core.redis.facade.HomoRedisPool;
import com.homo.core.redis.impl.HomoJedisCluster;
import com.homo.core.utils.spring.GetBeanUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.util.StringUtils;
import redis.clients.jedis.HostAndPort;
import redis.clients.jedis.JedisCluster;
import redis.clients.jedis.JedisPoolConfig;

import java.util.HashSet;
import java.util.Set;

;

@Log4j2
public class HomoJedisClusterPoolCreater {
    public static HomoRedisPool createPool(){
        try {
            RedisInfoHolder redisInfoHolder = GetBeanUtil.getBean(RedisInfoHolder.class);
            if(StringUtils.isEmpty(redisInfoHolder.getUrl())){
                log.warn("jedisUrl is null , could not init the tpfJedisPool");
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
            log.info("HomoJedisClusterPool create config_{}",config);
            JedisCluster jedisCluster;
            Set<HostAndPort> hostAndPorts = parseNodesHostAndPort(redisInfoHolder.getUrl());
            if(StringUtils.isEmpty(redisInfoHolder.getAuth())){
                jedisCluster = new JedisCluster(hostAndPorts,redisInfoHolder.getTimeOutMs(),redisInfoHolder.getSoTimeOut(),redisInfoHolder.getMaxAttemps(),config);
            }else{
                jedisCluster = new JedisCluster(hostAndPorts,redisInfoHolder.getTimeOutMs(),redisInfoHolder.getSoTimeOut(),redisInfoHolder.getMaxAttemps(), redisInfoHolder.getAuth(), config);
            }
            HomoJedisCluster homoJedisCluster = new HomoJedisCluster(jedisCluster);
            return homoJedisCluster;
        }catch (Exception e){
            log.error("HomoJedisClusterPool create error_{]", e);
            e.printStackTrace();
            return null;
        }
    }

    private static Set<HostAndPort> parseNodesHostAndPort(String nodes) {
        Set<HostAndPort> hostAndPorts = new HashSet<HostAndPort>();
        if (!StringUtils.isEmpty(nodes)) {
            String[] hosts = nodes.split(",");
            if (hosts.length == 1) {
                hosts = nodes.split(";");
            }
            int hostLength = hosts.length;
            for (String n : hosts) {
                String[] hs = n.split("\\:");
                if (hs.length == 2) {
                    String host = hs[0];
                    int port = Integer.parseInt(hs[1]);
                    hostAndPorts.add(new HostAndPort(host, port));
                }
            }
        }
        return hostAndPorts;
    }
}
