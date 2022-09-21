package com.homo.core.configurable.rpc;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Data
@ToString
@Configurable
public class RpcClientProperties {
    @Value("${homo.rpc.client.isDirector:true}")
    private boolean isDirector;
    @Value("${homo.rpc.client.checkDelaySecond:0}")
    private int checkDelaySecond;
    @Value("${homo.rpc.client.checkPeriodSecond:5}")
    private int checkPeriodSecond;
    @Value("${homo.rpc.client.channel.workerThread:2}")
    private int workerThread;
    @Value("${homo.rpc.client.channel.messageMaxSize:5242880}")//5MB
    private int messageMaxSize;
    @Value("${homo.rpc.client.channel.channelKeepLiveMillsSecond:5000}")
    private int channelKeepLiveMillsSecond;
    @Value("${homo.rpc.client.channel.channelKeepLiveTimeoutMillsSecond:5000}")
    private int channelKeepLiveTimeoutMillsSecond;
}
