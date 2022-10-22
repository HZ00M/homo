package com.homo.core.configurable.rpc;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
public class RpcGrpcServerProperties {
    @Value("${homo.rpc.server.grpc.thread.size:3}")
    private int corePoolSize;
    @Value("${homo.rpc.server.grpc.thread.keepLive:0}")
    private int keepLive;
    @Value("${homo.rpc.server.grpc.boss.thread.size:1}")
    private int boosThreadSize;
    @Value("${homo.rpc.server.grpc.worker.thread.size:2}")
    private int workerThreadSize;
    @Value("${homo.rpc.server.grpc.message.maxInboundMessageSize:5242880}")//5M
    private int maxInboundMessageSize;
    @Value("${homo.rpc.grpc.server.message.permitKeepAliveTime:5000}")
    private int permitKeepAliveTime;
}
