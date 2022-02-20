package com.homo.core.configurable.mongo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

@ConfigurationProperties(prefix = "homo.mongo")
@Data
public class MongoDriverProperties {
    @Value("${connString:mongodb://127.0.0.1:27017}")
    private String connString;
    @Value("${database:homo_storage}")
    private String database;
    @Value("${connPool.minSize:1}")
    private Integer minSize;
    @Value("${connPool.maxSize:100}")
    private Integer maxSize;
    @Value("${connPool.maxWaitTime:100}")
    private Long maxWaitTime;
    @Value("${connPool.maxConnectionIdleTime:10000}")
    private Long maxConnectionIdleTime;
    @Value("${connPool.maxConnectionLifeTime:60000}")
    private Long maxConnectionLifeTime;
}
