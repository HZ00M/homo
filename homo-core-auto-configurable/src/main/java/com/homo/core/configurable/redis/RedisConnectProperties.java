package com.homo.core.configurable.redis;

import lombok.Data;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
public class RedisConnectProperties {
    @Value("${redis.public.namespace:homo_redis_config}")
    private String publicNamespace;
    @Value("${redis.private.namespace:redis_connect_info}")
    private String privateNamespace;
    @Value("${redis.type:LETTUCE_POOL}")
    private String redisType;
}
