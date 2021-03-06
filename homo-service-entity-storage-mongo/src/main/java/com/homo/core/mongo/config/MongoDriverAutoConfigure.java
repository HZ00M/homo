package com.homo.core.mongo.config;

import com.homo.core.configurable.mongo.MongoDriverProperties;
import com.homo.core.facade.document.EntityStorageDriver;
import com.homo.core.mongo.storage.MongoEntityStorageDriverImpl;
import com.homo.core.mongo.util.MongoHelper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Lazy;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

;

@Log4j2
@EnableConfigurationProperties({MongoDriverProperties.class})
@Configuration
public class MongoDriverAutoConfigure {
    @Autowired
    MongoDriverProperties mongoDriverProperties;
    @Autowired
    @Lazy
    private MongoClient mongoClient;

    @Bean("mongoClient")
    public MongoClient mongoClient() {
        log.info("register bean mongoClient");
        log.info("connString: {}\ndatabase: {}", mongoDriverProperties.getConnString(), mongoDriverProperties.getDatabase());

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoDriverProperties.getConnString()))
                .applyToConnectionPoolSettings(builder -> {
                    builder.minSize(mongoDriverProperties.getMinSize())
                            .maxSize(mongoDriverProperties.getMaxSize())
                            .maxWaitTime(mongoDriverProperties.getMaxWaitTime(), TimeUnit.MILLISECONDS)
                            .maxConnectionIdleTime(mongoDriverProperties.getMaxConnectionIdleTime(), TimeUnit.MILLISECONDS)
                            .maxConnectionLifeTime(mongoDriverProperties.getMaxConnectionLifeTime(), TimeUnit.MILLISECONDS);
                })
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        return mongoClient;
    }


    @Bean("mongoHelper")
    @DependsOn("mongoClient")
    public MongoHelper mongoHelper(MongoClient mongoClient) {
        log.info("register bean mongoHelper");
        MongoHelper mongoHelper = new MongoHelper(mongoDriverProperties, mongoClient);
        mongoHelper.init();
        return mongoHelper;
    }

    @Bean
    @DependsOn("mongoHelper")
    public EntityStorageDriver entityStorageDriver() {
        log.info("register bean mongoHelper");
        return new MongoEntityStorageDriverImpl();
    }


    @PreDestroy
    public void destroy() throws Exception {
        log.info("mongoClient destroy !!!");
        mongoClient.close();
    }
}
