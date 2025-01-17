package com.homo.core.rpc.grpc.config;

import com.homo.core.configurable.rpc.RpcGrpcClientProperties;
import com.homo.core.configurable.rpc.RpcGrpcServerProperties;
import com.homo.core.facade.rpc.RpcClientFactory;
import com.homo.core.facade.rpc.RpcServerFactory;
import com.homo.core.rpc.grpc.GrpcRpcServerFactory;
import com.homo.core.rpc.grpc.GrpcRpcClientFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Slf4j
@Import({RpcGrpcClientProperties.class, RpcGrpcServerProperties.class})
public class GrpcRpcAutoConfiguration {

    @Bean("grpcRpcClientFactory")
    public RpcClientFactory grpcRpcClientFactory(){
        log.info("register bean GrpcRpcClientFactory");
        return new GrpcRpcClientFactory();
    }

    @Bean("grpcRpcServerFactory")
    public RpcServerFactory grpcRpcServerFactory(){
        log.info("register bean grpcRpcServerFactory");
        return new GrpcRpcServerFactory();
    }
}
