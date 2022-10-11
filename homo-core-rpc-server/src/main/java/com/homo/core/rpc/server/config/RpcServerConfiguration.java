package com.homo.core.rpc.server.config;

import com.homo.core.rpc.server.RpcServerMgr;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Log4j2
public class RpcServerConfiguration {

    @Bean("rpcServerMgr")
    public RpcServerMgr rpcServerMgr() {
        log.info("register bean rpcServerMgr");
        return new RpcServerMgr();
    }
}
