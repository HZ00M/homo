package com.homo.core.gate.config;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.gate.GateServerMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Slf4j
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
