package com.homo.core.rpc.server.config;

import com.homo.core.rpc.server.RpcServerMgr;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class RpcServerConfiguration {

    @Bean("rpcServerMgr")
    public RpcServerMgr rpcServerMgr() {
        log.info("register bean rpcServerMgr");
        return new RpcServerMgr();
    }
}
