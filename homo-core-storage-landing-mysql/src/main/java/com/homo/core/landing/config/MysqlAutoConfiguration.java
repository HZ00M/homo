package com.homo.core.landing.config;

import com.homo.core.facade.storege.landing.LandingDriver;
import com.homo.core.landing.DataLandingProcess;
import com.homo.core.landing.MysqlLandingDriver;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;

@Slf4j
@AutoConfiguration
@MapperScan("com.homo.core.landing.mapper")
public class MysqlAutoConfiguration {

    @Bean("dbDataHolder")
    @DependsOn({"homoRedisPool","dataObjMapper"})
    public LandingDriver dbDataHolder(){
        log.info("register bean dbDataHolder actual MysqlLoadDataHolder");
        return new MysqlLandingDriver();
    }

    @Bean("dataLandingProcess")
    @DependsOn("homoRedisPool")
    public DataLandingProcess dataLandingProcess(){
        log.info("register bean dataLandingProcess");
        return new DataLandingProcess();
    }
}
