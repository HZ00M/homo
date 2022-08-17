package com.homo.core.landing.config;

import com.homo.core.facade.storege.landing.DBDataHolder;
import com.homo.core.landing.DataLandingProcess;
import com.homo.core.landing.MysqlLoadDataHolder;
import lombok.extern.log4j.Log4j2;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Log4j2
@Configuration
@MapperScan("com.homo.core.landing.mapper")
public class MysqlAutoConfiguration {

    @Bean("dbDataHolder")
    @DependsOn({"homoRedisPool","dataObjMapper"})
    public DBDataHolder dbDataHolder(){
        log.info("register bean dbDataHolder actual MysqlLoadDataHolder");
        return new MysqlLoadDataHolder();
    }

    @Bean("dataLandingProcess")
    @DependsOn("homoRedisPool")
    public DataLandingProcess dataLandingProcess(){
        log.info("register bean dataLandingProcess");
        return new DataLandingProcess();
    }
}
