package com.homo.core.landing.config;

import com.homo.core.facade.storege.landing.DBDataHolder;
import com.homo.core.landing.MysqlLoadDataHolder;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Slf4j
@Configuration
@MapperScan("com.homo.core.landing.mapper")
public class MysqlAutoConfiguration {

    @Bean("dbDataHolder")
    @DependsOn({"homoRedisPool","ISchemeMapper"})
    public DBDataHolder dbDataHolder(){
        log.info("register bean dbDataHolder");
        return new MysqlLoadDataHolder();
    }
}
