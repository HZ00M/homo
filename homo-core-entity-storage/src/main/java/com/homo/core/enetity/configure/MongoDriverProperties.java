package com.homo.core.enetity.configure;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "homo")
@Configuration
@Data
public class MongoDriverProperties {
    @Value("${mongo.connString:mongodb://127.2.0.1::27017}")
    private String connString;
    @Value("${mongo.database:homo_storage}")
    private String database;
}
