package com.homo.core.configurable.gate;

import lombok.Data;
import lombok.ToString;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.beans.factory.annotation.Value;

@Configurable
@Data
@ToString
public class GateProperties {
    @Value("${homo.gate.idle.time:60}")
    public  Integer appId;
    @Value("${homo.gate.netty.worker.thread.num:1}")
    public  Integer workNum;
    @Value("${homo.gate.netty.boss.thread.num:1}")
    public  Integer bossNum;
}
