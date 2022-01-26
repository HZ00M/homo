package com.homo.core.configurable.mongo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "homo.mongo")
@Data
public class MongoDriverProperties {
    @Value("${connString:mongodb://127.0.0.1:27017}")
    private String connString;
    @Value("${database:homo_storage}")
    private String database;
}
