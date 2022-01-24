package com.homo.core.configurable.redis;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "homo.jedis.cluster")
@Configuration
@Data
public class JedisClusterProperties {
    @Value("${url}")
    private String url;
    @Value("${auth}")
    private String auth;

}
