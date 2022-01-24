package com.homo.core.configurable.mongo;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "homo.mongo")
@Configuration
@Data
public class MongoDriverProperties {
    @Value("${connString:mongodb://127.2.0.1::27017}")
    private String connString;
    @Value("${database:homo_storage}")
    private String database;
}
