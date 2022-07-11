package com.homo.service.dirty.config;

import com.homo.core.configurable.dirty.DirtyProperties;
import com.homo.core.facade.storege.dirty.DirtyDriver;
import com.homo.core.facade.storege.dirty.DirtyHelper;
import com.homo.service.dirty.RedisDirtyDriver;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

;


@Log4j2
@Import({DirtyProperties.class})
@Configuration
public class DirtyAutoConfiguration {

    @Autowired
    DirtyProperties dirtyProperties;

    @Bean("dirtyDriver")
    @DependsOn({"homoLockDriver"})
    public DirtyDriver dirtyDriver(){
        log.info("register bean dirtyDriver");
        DirtyHelper.init(dirtyProperties);
        return new RedisDirtyDriver();
    }

}
