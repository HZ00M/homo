package com.homo.core.configurable.rpc;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
public class RpcGrpcClientProperties {
    /**
     * 是否使用默认线程池执行
     */
    @Value("${homo.rpc.client.isDirector:true}")
    private boolean isDirector;
    /**
     * 检查目标服可用列表延迟时间
     */
    @Value("${homo.rpc.client.checkDelaySecond:0}")
    private int checkDelaySecond;
    /**
     * 检查目标服可用列表频率
     */
    @Value("${homo.rpc.client.checkPeriodSecond:5}")
    private int checkPeriodSecond;
    /**
     * netty客户端工作线程
     */
    @Value("${homo.rpc.client.channel.workerThread:2}")
    private int workerThread;
    /**
     * 消息最大长度，默认5mb
     */
    @Value("${homo.rpc.client.channel.messageMaxSize:5242880}")//5MB
    private int messageMaxSize;
    /**
     * 连接空闲保活时间
     */
    @Value("${homo.rpc.client.channel.channelKeepLiveMillsSecond:5000}")
    private int channelKeepLiveMillsSecond;
    /**
     * 客户端连接超时时间
     */
    @Value("${homo.rpc.client.channel.channelKeepLiveTimeoutMillsSecond:5000}")
    private int channelKeepLiveTimeoutMillsSecond;
}
