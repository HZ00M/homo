package com.homo.core.gate.tcp.config;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.configurable.gate.GateTcpProperties;
import com.homo.core.facade.gate.GateDriver;
import com.homo.core.gate.tcp.TcpGateDriver;
import com.homo.core.gate.tcp.handler.TailHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Slf4j
@Import({GateTcpProperties.class, GateCommonProperties.class})
public class TcpGateAutoConfiguration {
    @Autowired
    private GateTcpProperties gateTcpProperties;
    @Autowired
    private GateCommonProperties gateCommonProperties;

    @Bean("gateDriver")
    public GateDriver gateDriver(){
        log.info("gateDriver bean register");
        TcpGateDriver tcpGateDriver = new TcpGateDriver();
        tcpGateDriver.registerAfterHandler(new TailHandler(tcpGateDriver));
        return tcpGateDriver;
    }
}
