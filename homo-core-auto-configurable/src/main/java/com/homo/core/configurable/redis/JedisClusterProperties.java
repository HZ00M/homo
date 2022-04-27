package com.homo.core.configurable.redis;

import com.homo.core.configurable.NameSpaceConstant;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = NameSpaceConstant.JEDIS_CLUSTER)
@Data
public class JedisClusterProperties {
    @Value("${url:}")
    private String url;
    @Value("${auth:}")
    private String auth;

}
