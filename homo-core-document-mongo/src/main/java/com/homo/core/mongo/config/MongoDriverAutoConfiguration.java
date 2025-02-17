package com.homo.core.mongo.config;

import com.homo.core.configurable.mongo.MongoDriverProperties;
import com.homo.core.facade.document.DocumentStorageDriver;
import com.homo.core.mongo.storage.MongoDocumentStorageDriverImpl;
import com.homo.core.mongo.util.MongoHelper;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.*;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;


@Slf4j
@Import({MongoDriverProperties.class})
@AutoConfiguration
public class MongoDriverAutoConfiguration {
    @Autowired
    MongoDriverProperties mongoDriverProperties;
    @Autowired
    @Lazy
    private MongoClient mongoClient;

    public WriteConcern getWriteConcern() {
        if (mongoDriverProperties.getWriteConcern().equalsIgnoreCase("UNACKNOWLEDGED")) {
            return WriteConcern.UNACKNOWLEDGED;
        } else if (mongoDriverProperties.getWriteConcern().equalsIgnoreCase("ACKNOWLEDGED")) {
            return WriteConcern.ACKNOWLEDGED;
        } else if (mongoDriverProperties.getWriteConcern().equalsIgnoreCase("JOURNALED")) {
            return WriteConcern.JOURNALED;
        } else if (mongoDriverProperties.getWriteConcern().equalsIgnoreCase("MAJORITY")) {
            return WriteConcern.MAJORITY;
        } else if (mongoDriverProperties.getWriteConcern().equalsIgnoreCase("W1")) {
            return WriteConcern.W1;
        } else if (mongoDriverProperties.getWriteConcern().equalsIgnoreCase("W2")) {
            return WriteConcern.W2;
        } else if (mongoDriverProperties.getWriteConcern().equalsIgnoreCase("W3")) {
            return WriteConcern.W3;
        } else {
            return WriteConcern.ACKNOWLEDGED;
        }
    }

    public ReadPreference getReadPreference() {
        if (mongoDriverProperties.getReadPreference().equalsIgnoreCase(ReadPreference.primary().getName())) {
            return ReadPreference.primary();
        } else if (mongoDriverProperties.getReadPreference().equalsIgnoreCase(ReadPreference.secondary().getName())) {
            return ReadPreference.secondary();
        } else if (mongoDriverProperties.getReadPreference().equalsIgnoreCase(ReadPreference.primaryPreferred().getName())) {
            return ReadPreference.primaryPreferred();
        } else if (mongoDriverProperties.getReadPreference().equalsIgnoreCase(ReadPreference.secondaryPreferred().getName())) {
            return ReadPreference.secondaryPreferred();
        } else if (mongoDriverProperties.getReadPreference().equalsIgnoreCase(ReadPreference.nearest().getName())) {
            return ReadPreference.nearest();
        }else {
            return ReadPreference.primary();
        }
    }

    @Bean("mongoClient")
    public MongoClient mongoClient() {
        log.info("register bean mongoClient");
        log.info("connString: {}\ndatabase: {}", mongoDriverProperties.getConnString(), mongoDriverProperties.getDatabase());

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoDriverProperties.getConnString()))
                .retryWrites(mongoDriverProperties.getRetryWrites())
                .writeConcern(getWriteConcern())
                .readPreference(getReadPreference())
                .applyToConnectionPoolSettings(builder -> {
                    builder.minSize(mongoDriverProperties.getMinSize())
                            .maxSize(mongoDriverProperties.getMaxSize())
                            .maxWaitTime(mongoDriverProperties.getMaxWaitTime(), TimeUnit.MILLISECONDS)
                            .maxConnectionIdleTime(mongoDriverProperties.getMaxConnectionIdleTime(),TimeUnit.MILLISECONDS)
                            .maxConnectionLifeTime(mongoDriverProperties.getMaxConnectionLifeTime(),TimeUnit.MILLISECONDS);
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
    public DocumentStorageDriver entityStorageDriver() {
        log.info("register bean mongoHelper");
        return new MongoDocumentStorageDriverImpl();
    }


    @PreDestroy
    public void destroy() throws Exception {
        log.info("mongoClient destroy !!!");
        mongoClient.close();
    }
}
