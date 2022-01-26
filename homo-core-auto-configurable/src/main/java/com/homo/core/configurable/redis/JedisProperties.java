package com.homo.core.configurable.redis;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "homo.jedis")
@Data
public class JedisProperties {
    @Value("${homo.jedis.url:}")
    private String url;
    @Value("${auth:}")
    private String auth;
    @Value("${port:6379}")
    private Integer port ;
}
