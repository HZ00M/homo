package com.homo.core.gate.config;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.facade.gate.GateDriver;
import com.homo.core.gate.GateServerMgr;
import lombok.extern.log4j.Log4j2;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@Configuration
@Log4j2
@Import({ GateCommonProperties.class})
public class GateAutoConfiguration {

    @DependsOn("gateDriver")
    @Bean
    public GateServerMgr gateServerMgr(){
        log.info("GateServerMgr bean register");
        GateServerMgr gateServerMgr = new GateServerMgr();
        return gateServerMgr;
    }
}
