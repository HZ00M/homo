package com.homo.core.configurable.gate;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
@ToString
public class GateTcpProperties {
    /**
     * netty worker线程数
     */
    @Value("${homo.gate.netty.worker.thread.num:1}")
    public  Integer workNum;
    /**
     * netty boss线程数
     */
    @Value("${homo.gate.netty.boss.thread.num:1}")
    public  Integer bossNum;
    /**
     * 连接读空闲
     */
    @Value("${homo.gate.netty.channel.readerIdleTime:180}")
    public  Integer readerIdleTime;
    /**
     * 连接写空闲
     */
    @Value("${homo.gate.netty.channel.writerIdleTime:180}")
    public  Integer writerIdleTime;
    /**
     * 连接总空闲
     */
    @Value("${homo.gate.netty.channel.allIdleTime:180}")
    public  Integer allIdleTime;
}
