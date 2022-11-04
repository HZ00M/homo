package com.homo.core.gate.tcp.config;

import com.homo.core.configurable.gate.GateCommonProperties;
import com.homo.core.configurable.gate.GateTcpProperties;
import com.homo.core.facade.gate.GateDriver;
import com.homo.core.gate.tcp.GateMessagePackage;
import com.homo.core.gate.tcp.TcpGateDriver;
import com.homo.core.gate.tcp.handler.TailHandler;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Log4j2
@Import({GateTcpProperties.class, GateCommonProperties.class})
public class TcpGateAutoConfiguration {
    @Autowired
    private GateTcpProperties gateTcpProperties;
    @Autowired
    private GateCommonProperties gateCommonProperties;

    @Bean("gateDriver")
    public GateDriver tcpGateDriver(){
        log.info("gateDriver bean register");
        TcpGateDriver<GateMessagePackage> tcpGateDriver = new TcpGateDriver<>();
        tcpGateDriver.registerAfterHandler(new TailHandler<>(tcpGateDriver));
        return tcpGateDriver;
    }
}
