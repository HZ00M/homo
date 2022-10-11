package com.homo.core.rpc.grpc.config;

import com.homo.core.configurable.rpc.RpcClientProperties;
import com.homo.core.configurable.rpc.RpcServerProperties;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.facade.rpc.RpcServerFactory;
import com.homo.core.rpc.grpc.RpcClientFactoryGrpcImpl;
import com.homo.core.rpc.grpc.RpcServerFactoryGrpcImpl;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Log4j2
@Import({RpcClientProperties.class, RpcServerProperties.class})
public class GrpcRpcAutoConfiguration {

    @Bean("grpcRpcClientFactory")
    public RpcClientFactory rpcClientFactory(){
        log.info("register bean grpcRpcClientFactory");
        return new RpcClientFactoryGrpcImpl();
    }

    @Bean("grpcRpcServerFactory")
    public RpcServerFactory rpcServerFactory(){
        log.info("register bean grpcRpcServerFactory");
        return new RpcServerFactoryGrpcImpl();
    }
}