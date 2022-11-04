package com.homo.core.configurable.gate;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
@ToString
public class GateTcpProperties {
    @Value("${homo.gate.idle.time:60}")
    public  Integer appId;
    @Value("${homo.gate.netty.worker.thread.num:1}")
    public  Integer workNum;
    @Value("${homo.gate.netty.boss.thread.num:1}")
    public  Integer bossNum;
    @Value("${homo.gate.netty.channel.readerIdleTime:180}")
    public  Integer readerIdleTime;
    @Value("${homo.gate.netty.channel.writerIdleTime:180}")
    public  Integer writerIdleTime;
    @Value("${homo.gate.netty.channel.allIdleTime:180}")
    public  Integer allIdleTime;
}
