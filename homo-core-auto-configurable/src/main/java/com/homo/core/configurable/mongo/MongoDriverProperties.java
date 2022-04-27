package com.homo.core.configurable.mongo;

import com.homo.core.configurable.NameSpaceConstant;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = NameSpaceConstant.MONGO)
@Data
public class MongoDriverProperties {
    @Value("${connString:mongodb://127.0.0.1:27017}")
    private String connString;
    @Value("${database:homo_storage}")
    private String database;
    @Value("${minSize:1}")
    private Integer minSize;
    @Value("${maxSize:100}")
    private Integer maxSize;
    @Value("${maxWaitTime:100}")
    private Long maxWaitTime;
    @Value("${maxConnectionIdleTime:10000}")
    private Long maxConnectionIdleTime;
    @Value("${maxConnectionLifeTime:60000}")
    private Long maxConnectionLifeTime;
}
