package com.homo.core.rpc.client.config;

import com.homo.core.rpc.client.RpcClientMgr;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@Log4j2
public class RpcClientAutoConfiguration {

    @DependsOn({"serviceStateMgr"})
    @Bean("rpcClientMgr")
    public RpcClientMgr rpcClientMgr(){
        log.info("register bean rpcClientMgr");
        return new RpcClientMgr();
    }
}
