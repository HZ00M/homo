package com.homo.core.configurable.redis;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "homo.jedis")
@Configuration
@Data
public class JedisProperties {
    @Value("${url}")
    private String url;
    @Value("${auth}")
    private String auth;
    @Value("${port:6379}")
    Integer port ;
}
