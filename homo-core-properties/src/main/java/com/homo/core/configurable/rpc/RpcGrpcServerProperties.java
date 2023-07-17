package com.homo.core.configurable.rpc;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
public class RpcGrpcServerProperties {
    /**
     * server核心线程数
     */
    @Value("${homo.rpc.server.grpc.thread.size:3}")
    private int corePoolSize;
    /**
     * 核心线程池保活时间
     */
    @Value("${homo.rpc.server.grpc.thread.keepLive:0}")
    private int keepLive;
    /**
     * boos线程数
     */
    @Value("${homo.rpc.server.grpc.boss.thread.size:1}")
    private int boosThreadSize;
    /**
     * 工作这线程数
     */
    @Value("${homo.rpc.server.grpc.worker.thread.size:2}")
    private int workerThreadSize;
    /**
     * 最大包限制，默认5MB
     */
    @Value("${homo.rpc.server.grpc.message.maxInboundMessageSize:5242880}")//5M
    private int maxInboundMessageSize;
    /**
     * 请求保活时间
     */
    @Value("${homo.rpc.grpc.server.message.permitKeepAliveTime:5000}")
    private int permitKeepAliveTime;
}
