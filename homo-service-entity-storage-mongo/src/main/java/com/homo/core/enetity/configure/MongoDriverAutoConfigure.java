package com.homo.core.enetity.configure;

import com.homo.core.configurable.mongo.MongoDriverProperties;
import com.homo.core.enetity.storage.MongoEntityStorageDriverImpl;
import com.homo.core.enetity.util.MongoHelper;
import com.homo.core.facade.document.EntityStorageDriver;
import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

@Slf4j
@EnableConfigurationProperties({MongoDriverProperties.class})
@Configuration
public class MongoDriverAutoConfigure {
    @Autowired
    MongoDriverProperties mongoDriverProperties;
    @Bean("mongoClient")
    public MongoClient mongoClient() {
        log.info("MongoClient post construct");
        log.info("connString: {}\ndatabase: {}", mongoDriverProperties.getConnString(), mongoDriverProperties.getDatabase());

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(mongoDriverProperties.getConnString()))
                .build();
        MongoClient mongoClient = MongoClients.create(settings);
        return mongoClient;
    }

    @Bean("mongoHelper")
    @DependsOn("mongoClient")
    public MongoHelper mongoHelper(MongoClient mongoClient){
        MongoHelper mongoHelper = new MongoHelper(mongoDriverProperties,mongoClient);
        mongoHelper.init();
        return mongoHelper;
    }

    @Bean
    @DependsOn("mongoHelper")
    public EntityStorageDriver entityStorageDriver(){
        return new MongoEntityStorageDriverImpl();
    }
}
